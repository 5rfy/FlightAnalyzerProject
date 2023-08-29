package org.example;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class FlightAnalyzer {
    public static void main(String[] args) {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject;
        JSONArray tickets;
        
        try {
            jsonObject = (JSONObject) parser.parse(new FileReader("/Users/egorsaprykin/Downloads/tickets.json"));
            tickets = (JSONArray) jsonObject.get("tickets");

        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }

        Map<String, Integer> minFlightTime = new HashMap<>();
        List<BigDecimal> flightPrice = new ArrayList<>();


        for (Object ticketObj : tickets) {
            JSONObject ticket = (JSONObject) ticketObj;

            String origin = (String) ticket.get("origin");
            String destination = (String) ticket.get("destination");
            String carrier = (String) ticket.get("carrier");

            if (origin.equals("VVO") && destination.equals("TLV")) {
                String departureTime = (String) ticket.get("departure_time");
                String arrivalTime = (String) ticket.get("arrival_time");

                int flightTime = calculateTimeDifferent(departureTime, arrivalTime);

                if (!minFlightTime.containsKey(carrier) || flightTime < minFlightTime.get(carrier)) {
                    minFlightTime.put(carrier, flightTime);
                }

                BigDecimal price = BigDecimal.valueOf((Long) ticket.get("price"));
                flightPrice.add(price);
            }
        }

        BigDecimal averagePrice = flightPrice.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(flightPrice.size()), 2, BigDecimal.ROUND_HALF_UP);

        BigDecimal medianPrice = calculateMedian(flightPrice);

        System.out.println("Minimum flight times between VVO and TLV:");
        minFlightTime.forEach((carrier, time) ->
                System.out.println(carrier + ": " + time + " minutes"));

        System.out.println("\nDifference between average and median flight prices:");
        System.out.println("Difference: " + (averagePrice.subtract(medianPrice)));
    }

    private static BigDecimal calculateMedian(List<BigDecimal> prices) {
        List<BigDecimal> sortedPrices = prices.stream().sorted().toList();
        int size = sortedPrices.size();

        if (size % 2 == 0) {
            int middle = size / 2;
            return (sortedPrices.get(middle - 1).add(sortedPrices.get(middle)))
                    .divide(BigDecimal.valueOf(2), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            return sortedPrices.get(size / 2);
        }
    }

    private static int calculateTimeDifferent(String departureTime, String arrivalTime) {

        int departureMinutes = Arrays.stream(departureTime.split(":"))
                .mapToInt(Integer::parseInt)
                .reduce((hours, minutes) -> hours * 60 + minutes).orElse(0);

        int arrivalMinutes = Arrays.stream(arrivalTime.split(":"))
                .mapToInt(Integer::parseInt)
                .reduce((hours, minutes) -> hours * 60 + minutes).orElse(0);

        return arrivalMinutes - departureMinutes;
    }
}