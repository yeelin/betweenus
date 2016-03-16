package com.example.yeelin.projects.betweenus.data.google.model;

/**
 * Created by ninjakiki on 2/26/16.
 */
public class DistanceMatrixResult {
    private final String status;
    private final String[] origin_addresses;
    private final String[] destination_addresses;
    //rows contains an array of Row objects; each Row contains an array of Element objects.
    private final Row[] rows;


    public DistanceMatrixResult(String status, String[] origin_addresses, String[] destination_addresses, Row[] rows) {
        this.status = status;
        this.origin_addresses = origin_addresses;
        this.destination_addresses = destination_addresses;
        this.rows = rows;
    }

    public String getStatus() {
        return status;
    }

    public String[] getOrigin_addresses() {
        return origin_addresses;
    }

    public String[] getDestination_addresses() {
        return destination_addresses;
    }

    public Row[] getRows() {
        return rows;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i=0; i<rows.length; i++) {
            final Row row = rows[i];
            final Element[] elements = row.getElements();
            //each elements array represent the travel times between 1 origin and multiple destinations
            final String origin = origin_addresses[i];

            for (int j=0; j<elements.length; j++) {
                //each item in the an elements array represents the travel time between 1 origin and 1 destination
                final String destination = destination_addresses[j];
                stringBuilder.append(String.format("Origin:%s, Dest:%s, Element:%s", origin, destination, elements[j]));
            }
        }
        return stringBuilder.toString();
    }

    public static class Row {
        private final Element[] elements;

        public Row(Element[] elements) {
            this.elements = elements;
        }

        public Element[] getElements() {
            return elements;
        }
    }
}
