package com.octopus;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/apis/custom.metrics.k8s.io")
public class OctopusMetricsServer {

    @GET
    @Path("v1beta1")
    @Produces(MediaType.APPLICATION_JSON)
    public String status() {
        return "{\"status\": \"healthy\"}";
    }

    @GET
    @Path("v1beta1/namespaces/{namespace}/services/{service}/tasks")
    @Produces(MediaType.APPLICATION_JSON)
    public String metric(@PathParam("namespace") final String namespace, @PathParam("service") final String service) {
        return "{\n"
            + "        kind: \"MetricValueList\",\n"
            + "        apiVersion: \"custom.metrics.k8s.io/v1beta1\",\n"
            + "        metadata: {\n"
            + "            selfLink: \"/apis/custom.metrics.k8s.io/v1beta1/\"\n"
            + "        },\n"
            + "        items: [\n"
            + "            {\n"
            + "                describedObject: {\n"
            + "                    kind: \"Service\",\n"
            + "                    namespace: \"default\",\n"
            + "                    name: \"octopus-metrics-server\",\n"
            + "                    apiVersion: \"/v1beta1\"\n"
            + "                },\n"
            + "                metricName: \"tasks\",\n"
            + "                timestamp: " + ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT ) + ",\n"
            + "                value: 3\n"
            + "            }\n"
            + "        ]\n"
            + "    })\n"
            + "}";
    }
}