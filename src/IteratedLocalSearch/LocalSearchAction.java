package IteratedLocalSearch;

import entities.Car;
import entities.Customer;
import entities.Route;
import services.RouteServices;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class LocalSearchAction {

    private static Customer depot;
    private static ArrayList<Customer> firstBest = new ArrayList<>();
    private static ArrayList<Customer> secondBest = new ArrayList<>();
    private static double bestDistance;
    private static double totalBestDistance = 0;

    private static ArrayList<Customer> validCombination = new ArrayList<>();

    public static ArrayList<Customer> getValidCombination() {
        return validCombination;
    }

    public static void setValidCombination(ArrayList<Customer> validCombination) {
        LocalSearchAction.validCombination = validCombination;
    }

    public static double getBestDistance() {
        return bestDistance;
    }


    public static void execute(ArrayList<Route> routes, Customer depotOut) {
        depot = depotOut;
        for (Route firstRoute : routes) {
            for (Route secondRoute : routes) {
                if (firstRoute.getId() != secondRoute.getId()) {
                    makeSwapByOne(firstRoute, secondRoute);
                    /*if (firstRoute.getListOfCustomers().size() > 0 && secondRoute.getListOfCustomers().size() > 0) {
                        makeSwapByTwo(firstRoute, secondRoute);
                    }
                    */
                }

            }
        }
        for(Route route : routes){
            double distance = RouteServices.countDistance(route.getListOfCustomers(), depot);
            if(recombination(route.getListOfCustomers(), distance)){
                route.setListOfCustomers(getValidCombination());
                route.getCar().setDistance(RouteServices.countDistance(route.getListOfCustomers(), depot));
                route.setFinishDepot(route.getCar().getDistance());
                setNewStartServiceTimes(route, depot);
            }
        }

    }


    public static boolean recombination(ArrayList<Customer> customers, double distance){
        for (int i = 0; i < customers.size(); i ++){
            double newDistance = swap(i, customers, distance);
            if (newDistance != -1 && newDistance < distance ){
                distance = newDistance;
                return true;
            }
        }
        return false;
    }
    public static double swap(int i, ArrayList<Customer> customers, double distance){
        ArrayList<Customer> copyCustomers = new ArrayList<>(customers);
        for (int j = i; j < copyCustomers.size() - 1; j++){
            Customer secondCust = copyCustomers.remove(j+1);
            Customer firstCust = copyCustomers.remove(j);
            copyCustomers.add(j, secondCust);
            copyCustomers.add(j+1, firstCust);
            double newDistance = LSValidator.validateDistance(copyCustomers, depot);
            if (newDistance == -1 || newDistance > distance){
                return swap(j+1, copyCustomers, distance);
            } else if (newDistance < distance){
                setValidCombination(copyCustomers);
                return newDistance;
            }
        }
        return -1;
    }

    public static void makeSwapByOne(Route firstRoute, Route secondRoute) {
        ArrayList<Customer> firstCustomers = firstRoute.getListOfCustomers();
        ArrayList<Customer> firstCustomersCopy = new ArrayList<>(firstCustomers);
        ArrayList<Customer> secondCustomers = secondRoute.getListOfCustomers();
        ArrayList<Customer> secondCustomersCopy = new ArrayList<>(secondCustomers);
        setBests(firstCustomers, secondCustomers);
        bestDistance = RouteServices.countDistance(firstCustomers, depot) + RouteServices.countDistance(secondCustomers, depot);

        swapByOneCustomer(firstCustomers, secondCustomers, firstRoute.getCar(), secondRoute.getCar());
        firstRoute.setListOfCustomers(firstBest);
        secondRoute.setListOfCustomers(secondBest);

        firstRoute.getCar().setDistance(RouteServices.countDistance(firstBest, depot));
        secondRoute.getCar().setDistance(RouteServices.countDistance(secondBest, depot));

        firstRoute.setFinishDepot(firstRoute.getCar().getDistance());
        secondRoute.setFinishDepot(secondRoute.getCar().getDistance());

        setNewStartServiceTimes(firstRoute, depot);
        setNewStartServiceTimes(secondRoute, depot);

        totalBestDistance = totalBestDistance + bestDistance;
    }

    public static void swapByOneCustomer(ArrayList<Customer> firstCustomers, ArrayList<Customer> secondCustomers, Car firstCar, Car secondCar) {
        ArrayList<Customer> firstCustomersCopy = new ArrayList<>(firstCustomers);
        ArrayList<Customer> secondCustomersCopy = new ArrayList<>(secondCustomers);
        for (Customer firstCustomer : firstCustomers) {
            for (Customer secondCustomer : secondCustomers) {
                int firstIndex = firstCustomers.indexOf(firstCustomer);
                int secondIndex = secondCustomers.indexOf(secondCustomer);
                // System.out.println("Swap!");
                firstCustomersCopy.remove(firstIndex);
                secondCustomersCopy.remove(secondIndex);
                firstCustomersCopy.add(firstIndex, secondCustomer);
                secondCustomersCopy.add(secondIndex, firstCustomer);
                //validation
                boolean isValid = LSValidator.validate(firstCustomersCopy, secondCustomersCopy, depot, firstCar, secondCar);
                if (isValid) {
                    double newDistance = RouteServices.countDistance(firstCustomersCopy, depot) + RouteServices.countDistance(secondCustomersCopy, depot);
                    if (newDistance < bestDistance) {
                        setBests(firstCustomersCopy, secondCustomersCopy);
                        bestDistance = newDistance;
                        swapByOneCustomer(firstCustomersCopy, secondCustomersCopy, firstCar, secondCar);

                    } else {
                    }
                } else {
                    //System.out.println("Bad");
                }
                firstCustomersCopy.remove(firstIndex);
                secondCustomersCopy.remove(secondIndex);
                firstCustomersCopy.add(firstIndex, firstCustomer);
                secondCustomersCopy.add(secondIndex, secondCustomer);

            }

        }
    }

    public static void printToCheck(ArrayList<Customer> customers) {
        for (Customer customer : customers) {
            System.out.print(customer.getCustomerId() + " ");
        }
        System.out.println();
    }

    public static void setBests(ArrayList<Customer> firstCustomers, ArrayList<Customer> secondCustomers) {
        firstBest = new ArrayList<>(firstCustomers);
        secondBest = new ArrayList<>(secondCustomers);
    }

    public static void setNewStartServiceTimes(Route route, Customer depot) {
        ArrayList<Customer> customers = route.getListOfCustomers();
        Map<Customer, Double> customersAndStartServiceTime = new LinkedHashMap<>();
        double distance = 0;
        int size = customers.size();
        if (size > 0) {
            Customer first = customers.get(0);
            distance = RouteServices.countDistanceAndServiceTime(depot, first, distance);
            customersAndStartServiceTime.put(first, distance - first.getServiceTime());
        }
        for (int i = 0; i < size; i++) {
            if (i + 1 < size) {
                Customer first = customers.get(i);
                Customer second = customers.get(i + 1);
                distance = RouteServices.countDistanceAndServiceTime(first, second, distance);
                customersAndStartServiceTime.put(second, distance - second.getServiceTime());

            }
        }
        route.setCustomersAndStartServiceTime(customersAndStartServiceTime);
    }

    public static void makeSwapByTwo(Route firstRoute, Route secondRoute) {
        ArrayList<Customer> firstCustomers = firstRoute.getListOfCustomers();
        ArrayList<Customer> firstCustomersCopy = new ArrayList<>(firstCustomers);
        ArrayList<Customer> secondCustomers = secondRoute.getListOfCustomers();
        ArrayList<Customer> secondCustomersCopy = new ArrayList<>(secondCustomers);
        setBests(firstCustomers, secondCustomers);
        bestDistance = RouteServices.countDistance(firstCustomers, depot) + RouteServices.countDistance(secondCustomers, depot);
        swapByTwoCustomers(firstCustomers, secondCustomers, firstRoute.getCar(), secondRoute.getCar());

        firstRoute.setListOfCustomers(firstBest);
        secondRoute.setListOfCustomers(secondBest);

        firstRoute.getCar().setDistance(RouteServices.countDistance(firstBest, depot));
        secondRoute.getCar().setDistance(RouteServices.countDistance(secondBest, depot));

        firstRoute.setFinishDepot(firstRoute.getCar().getDistance());
        secondRoute.setFinishDepot(secondRoute.getCar().getDistance());

        setNewStartServiceTimes(firstRoute, depot);
        setNewStartServiceTimes(secondRoute, depot);
    }

    public static void swapByTwoCustomers(ArrayList<Customer> firstCustomers, ArrayList<Customer> secondCustomers, Car firstCar, Car secondCar) {
        ArrayList<Customer> firstCustomersCopy = new ArrayList<>(firstCustomers);
        ArrayList<Customer> secondCustomersCopy = new ArrayList<>(secondCustomers);
        for (int i = 0; i <= 5; i++) {
            int firstIndexFirstCustomer = (int) (Math.random() * firstCustomers.size());
            int secondIndexFirstCustomer = (int) (Math.random() * firstCustomers.size());
            while (firstIndexFirstCustomer == secondIndexFirstCustomer) {
                secondIndexFirstCustomer = (int) (Math.random() * firstCustomers.size());
            }
            Customer firstCustomerFromFirst = firstCustomersCopy.get(firstIndexFirstCustomer);
            Customer secondCustomerFromFirst = firstCustomersCopy.get(secondIndexFirstCustomer);

            int firstIndexSecondCustomer = (int) (Math.random() * secondCustomers.size());
            int secondIndexSecondCustomer = (int) (Math.random() * secondCustomers.size());
            while (firstIndexSecondCustomer == secondIndexSecondCustomer) {
                secondIndexSecondCustomer = (int) (Math.random() * secondCustomers.size());
            }

            Customer firstCustomerFromSecond = secondCustomersCopy.get(firstIndexSecondCustomer);
            Customer secondCustomerFromSecond = secondCustomersCopy.get(secondIndexSecondCustomer);
            // System.out.println("Swap!");

            firstCustomersCopy.remove(firstIndexFirstCustomer);
            firstCustomersCopy.add(firstIndexFirstCustomer, secondCustomerFromSecond);

            firstCustomersCopy.remove(secondIndexFirstCustomer);
            firstCustomersCopy.add(secondIndexFirstCustomer, firstCustomerFromSecond);


            secondCustomersCopy.remove(firstIndexSecondCustomer);
            secondCustomersCopy.add(firstIndexSecondCustomer, firstCustomerFromFirst);

            secondCustomersCopy.remove(secondIndexSecondCustomer);
            secondCustomersCopy.add(secondIndexSecondCustomer, secondCustomerFromFirst);


            //validation
            boolean isValid = LSValidator.validate(firstCustomersCopy, secondCustomersCopy, depot, firstCar, secondCar);
            if (isValid) {
                double newDistance = RouteServices.countDistance(firstCustomersCopy, depot) + RouteServices.countDistance(secondCustomersCopy, depot);
                if (newDistance < bestDistance) {
                    setBests(firstCustomersCopy, secondCustomersCopy);
                    bestDistance = newDistance;
                    swapByTwoCustomers(firstCustomersCopy, secondCustomersCopy, firstCar, secondCar);

                } else {
                }
            } else {
            }
            firstCustomersCopy.remove(firstIndexFirstCustomer);
            firstCustomersCopy.add(firstIndexFirstCustomer, firstCustomerFromFirst);

            firstCustomersCopy.remove(secondIndexFirstCustomer);
            firstCustomersCopy.add(secondIndexFirstCustomer, secondCustomerFromFirst);

            secondCustomersCopy.remove(firstIndexSecondCustomer);
            secondCustomersCopy.add(firstIndexSecondCustomer, firstCustomerFromSecond);

            secondCustomersCopy.remove(secondIndexSecondCustomer);
            secondCustomersCopy.add(secondIndexSecondCustomer, secondCustomerFromSecond);

        }

    }
}
