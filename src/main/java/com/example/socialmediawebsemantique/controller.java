package com.example.socialmediawebsemantique;

import com.example.socialmediawebsemantique.tools.jenaEngine;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.util.FileManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab.NS;


@RestController

public class controller {

    Model model = jenaEngine.readModel("data/oneZero.owl");


    @GetMapping("/OnlineEventSearch")
    @CrossOrigin(origins = "*")
    public String getOnlineEvent(
            @RequestParam(value = "domain", required = false) String domain
    ) {
        String NS = "";
        // lire le model a partir d'une ontologie
        JsonArray jsonArray = null;
        if (model != null) {
            // lire le Namespace de l�ontologie
            NS = model.getNsPrefixURI("");

            // apply our rules on the owlInferencedModel
            Model inferedModel = jenaEngine.readInferencedModelFromRuleFile(model, "data/rules.txt");

            // query on the model after inference


            String queryStr = FileManager.get().readWholeFileAsUTF8("data/query_OnlineEvent.txt");


            // Set the value of ?domainParam
            if (domain != null && !domain.isEmpty()) {
                // Replace the parameter placeholder with the actual domain value
                queryStr = queryStr.replace("?titleParam", '\"' + domain + '\"');
            } else {
                // If domain is not provided, remove the parameter and the FILTER condition from the query
                queryStr = queryStr.replace("FILTER (?titleParam != \"\" && ?title = ?titleParam)", "");
            }

            System.out.println(queryStr);
            // Execute the query
            Query query = QueryFactory.create(queryStr);
            QueryExecution qexec = QueryExecutionFactory.create(query, inferedModel);

            // Execute the query
            ResultSet results = qexec.execSelect();

            jsonArray = new JsonArray();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                JsonObject jsonObject = new JsonObject();
                jsonObject.put("OnlineEvent", solution.get("OnlineEvent").toString());
                jsonObject.put("id", solution.get("id").toString());
                jsonObject.put("title", solution.get("title").toString());
                jsonObject.put("description", solution.get("description").toString());
                jsonObject.put("date", solution.get("date").toString());
                jsonObject.put("type", solution.get("type").toString());

                jsonArray.add(jsonObject);
            }


        }

        // Convert the JSON to a string
        String jsonResult = jsonArray.toString();

        System.out.println(jsonResult);
        return jsonResult;


    }


    @GetMapping("/EventSearch")
    @CrossOrigin(origins = "*")
    public String getEvents(
            @RequestParam(value = "domain", required = false) String domain,
            @RequestParam(value = "Type", required = false) String Type,
            @RequestParam(value = "orderBy", required = false) String orderBy,
            @RequestParam (value = "attribute" ,required = false) String attribute,
            @RequestParam (value = "regexParam" , required = false) String regexParam


    ) {
        String NS = "";
        // lire le model a partir d'une ontologie
        JsonArray jsonArray = null;
        if (model != null) {
            // lire le Namespace de l�ontologie
            NS = model.getNsPrefixURI("");

            // apply our rules on the owlInferencedModel
            Model inferedModel = jenaEngine.readInferencedModelFromRuleFile(model, "data/rules.txt");

            // query on the model after inference
            /*OutputStream res =  JenaEngine.executeQueryFile(inferedModel, "data/query_Skill.txt");

            System.out.println(res);
            return res.toString();*/

            String queryStr = FileManager.get().readWholeFileAsUTF8("data/query_Event.txt");



            // Set the value of ?domainParam
            if (domain != null && !domain.isEmpty()) {
                // Replace the parameter placeholder with the actual domain value
                queryStr = queryStr.replace("?titleParam", '\"' + domain + '\"');
            } else {
                // If domain is not provided, remove the parameter and the FILTER condition from the query
                queryStr = queryStr.replace("FILTER (?titleParam != \"\" && ?title = ?titleParam)", "");
            }

            if (orderBy != null && !orderBy.isEmpty() && Type != null && !Type.isEmpty()
                    && (Type.toUpperCase().equals("ASC") || Type.toUpperCase().equals("DESC"))) {
                queryStr = queryStr.replace("?orderBy",  '?'+orderBy.toLowerCase() );
                queryStr = queryStr.replace("?Type",  Type.toUpperCase() );
            } else {
                queryStr = queryStr.replace("ORDER BY ?Type(?orderBy)", "");
            }

            if (regexParam != null && !regexParam.isEmpty() && attribute != null && !attribute.isEmpty()) {
                queryStr = queryStr.replace("?regexParam", '\"' + regexParam + '\"');
                queryStr = queryStr.replace("?attribute",  '?'+attribute.toLowerCase() );
            } else {
                queryStr = queryStr.replace("FILTER regex(?attribute, ?regexParam, \"i\")", "");
            }


            System.out.println(queryStr);
            // Execute the query
            // Exécuter la requête pour les événements en ligne
            Query query = QueryFactory.create(queryStr);
            QueryExecution qexec = QueryExecutionFactory.create(query, inferedModel);
            ResultSet results = qexec.execSelect();

// Créer un tableau JSON pour stocker les résultats de la requête sur les événements en ligne
            jsonArray = new JsonArray();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                JsonObject jsonObject = new JsonObject();
                jsonObject.put("Event", solution.get("Event").toString());
                String idValue = solution.get("id").toString();
                String cleanedId = idValue.replace("^^http://www.w3.org/2001/XMLSchema#integer", "");
                jsonObject.put("id", cleanedId);
                jsonObject.put("title", solution.get("title").toString());
                jsonObject.put("description", solution.get("description").toString());
                String dateValue = solution.get("date").toString();
                String cleanedDate = dateValue.replace("^^http://www.w3.org/2001/XMLSchema#dateTime", "");

                jsonObject.put("date", cleanedDate);
                jsonObject.put("type", solution.get("type").toString());
                jsonArray.add(jsonObject);
            }


// Convertir le JSON en une chaîne
            String jsonResult = jsonArray.toString();

            System.out.println(jsonResult);
            return jsonResult;


        }

        return null;


    }


    //Delete Event
    // Supprimer un événement par ID en utilisant une requête SPARQL
    @DeleteMapping("/deleteEvent")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> deleteEvent(@RequestParam("id") Integer id) {
        // Charger les données RDF depuis un fichier
        Model model = jenaEngine.readModel("data/oneZero.owl");

        // Créer un modèle Ont qui effectue des inférences
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, model);

        // Construire la requête SPARQL pour supprimer l'individu par ID
        String sparqlDeleteQuery = "PREFIX ns: <http://www.semanticweb.org/sadok/ontologies/2023/9/untitled-ontology-9#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "DELETE {\n" +
                "  ?Event rdf:type ns:Event;\n" +
                "         ns:id ?id;\n" +
                "         ns:title ?title;\n" +
                "         ns:date ?date;\n" +
                "         ns:description ?description;\n" +
                "         ns:type ?type.\n" +
                "} WHERE {\n" +
                "  ?Event rdf:type ns:Event;\n" +
                "         ns:id ?id;\n" +
                "         ns:title ?title;\n" +
                "         ns:date ?date;\n" +
                "         ns:description ?description;\n" +
                "         ns:type ?type.\n" +
                "  FILTER (?id = " + id + ")\n" +
                "}\n";

System.out.println(sparqlDeleteQuery);



        // Exécuter la requête SPARQL pour supprimer l'individu
        UpdateAction.parseExecute(sparqlDeleteQuery, ontModel);

        // Enregistrer le modèle RDF mis à jour
        try (OutputStream outputStream = Files.newOutputStream(Paths.get("data/oneZero.owl"))) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Échec de la suppression de l'événement.");
        }

        return ResponseEntity.status(HttpStatus.OK).body("Événement supprimé avec succès.");
    }
    @PostMapping("/createEvent")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> createEvent(@RequestBody EventModel eventRequest) throws ParseException {
        // Extract data from the EventRequest object
        Integer id = eventRequest.getId();
        String title = eventRequest.getTitle();
        String description = eventRequest.getDescription();
        String dateString = eventRequest.getDate();
        String type = eventRequest.getType();

        // Load RDF data from a file
        Model model = jenaEngine.readModel("data/oneZero.owl");

        // Create an OntModel for inferencing with the correct namespace
        String NS = "http://www.semanticweb.org/sadok/ontologies/2023/9/untitled-ontology-9#";
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, model);

        // Parse the date from the string
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = dateFormat.parse(dateString);

        // Create the RDF properties using the correct URIs
        String eventURI = NS + "Event_" + generateUniqueID();
        String dateURI = NS + "date";
        String typeURI = NS + "type";
        String titleURI = NS + "title";
        String idURI = NS + "id";
        String descriptionURI = NS + "description";

        // Create an individual for the new event with the appropriate URI
        Individual newEvent = ontModel.createIndividual(eventURI, ontModel.createClass(NS + "Event"));

        // Set the properties of the event using the correct URIs
        newEvent.addProperty(ontModel.getProperty(idURI), ontModel.createTypedLiteral(id, XSDDatatype.XSDinteger));
        newEvent.addProperty(ontModel.getProperty(titleURI), title);
        newEvent.addProperty(ontModel.getProperty(descriptionURI), description);
        newEvent.addProperty(ontModel.getProperty(dateURI), ontModel.createTypedLiteral(date, XSDDatatype.XSDdateTime));
        newEvent.addProperty(ontModel.getProperty(typeURI), type);

        // Save the updated RDF model
        try (OutputStream outputStream = Files.newOutputStream(Paths.get("data/oneZero.owl"))) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create the event.");
        }

        return ResponseEntity.status(HttpStatus.OK).body("Event created successfully.");
    }

    // Generate a unique ID for the new event
    private String generateUniqueID() {
        return String.valueOf(System.currentTimeMillis());
    }

    @PutMapping("/updateEvent")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> updateEvent(@RequestBody EventModel eventRequest, @RequestParam("id") Integer id) {
        // Load RDF data from a file
        Model model = jenaEngine.readModel("data/oneZero.owl");

        // Create an OntModel for inferencing with the correct namespace
        String NS = "http://www.semanticweb.org/sadok/ontologies/2023/9/untitled-ontology-9#";
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, model);

        // Define the properties and URIs to update
        String dateURI = NS + "date";
        String typeURI = NS + "type";
        String titleURI = NS + "title";
        String descriptionURI = NS + "description";

        // Use a prepared SPARQL query to avoid injection vulnerabilities and improve readability
        String sparqlFindQuery = String.format(
                "SELECT ?Event WHERE { ?Event <%s> ?id. FILTER (?id = %d) }",
                NS + "id",
                id
        );

        QueryExecution findQueryExec = QueryExecutionFactory.create(sparqlFindQuery, ontModel);
        ResultSet findResults = findQueryExec.execSelect();

        if (findResults.hasNext()) {
            // Individual with the specified ID exists, proceed with the update
            Individual existingEvent = ontModel.getIndividual(findResults.next().getResource("Event").getURI());

            // Extract the data to update from the request
            String newTitle = eventRequest.getTitle();
            String newDescription = eventRequest.getDescription();
            String newDateString = eventRequest.getDate();
            String newType = eventRequest.getType();

            // Parse the new date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            try {
                Date newDate = dateFormat.parse(newDateString);
                Literal newDateLiteral = ontModel.createTypedLiteral(newDate, XSDDatatype.XSDdateTime);

                // Update the properties of the existing event
                existingEvent.setPropertyValue(ontModel.getProperty(titleURI), ontModel.createLiteral(newTitle));
                existingEvent.setPropertyValue(ontModel.getProperty(descriptionURI), ontModel.createLiteral(newDescription));

                existingEvent.setPropertyValue(ontModel.getProperty(dateURI), newDateLiteral);
                existingEvent.setPropertyValue(ontModel.getProperty(typeURI), ontModel.createLiteral(newType));
            } catch (ParseException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid date format.");
            }

            // Save the updated RDF model
            try (OutputStream outputStream = Files.newOutputStream(Paths.get("data/oneZero.owl"))) {
                ontModel.write(outputStream, "RDF/XML-ABBREV");
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update the event.");
            }

            return ResponseEntity.status(HttpStatus.OK).body("Event updated successfully.");
        } else {
            // Individual with the specified ID doesn't exist
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event with ID " + id + " not found.");
        }
    }

    @GetMapping("/getEventById")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> getEventById(@RequestParam("id") Integer id) {
        // Load RDF data from a file
        Model model = jenaEngine.readModel("data/oneZero.owl");

        // Create an OntModel for inferencing with the correct namespace
        String NS = "http://www.semanticweb.org/sadok/ontologies/2023/9/untitled-ontology-9#";
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, model);

        // Define the properties and URIs to retrieve

        String dateURI = NS + "date";
        String typeURI = NS + "type";
        String titleURI = NS + "title";
        String descriptionURI = NS + "description";

        // Find the individual by its ID
        // Find the individual by its ID
        String sparqlFindQuery = "SELECT ?title ?description ?date ?type WHERE { " +
                "?Event <" + NS + "id> ?id. " +
                "  FILTER (?id = " + id + ")" +
                "?Event <" + titleURI + "> ?title. " +
                "?Event <" + descriptionURI + "> ?description. " +
                "?Event <" + dateURI + "> ?date. " +
                "?Event <" + typeURI + "> ?type. " +
                "}";



        QueryExecution findQueryExec = QueryExecutionFactory.create(sparqlFindQuery, ontModel);
        ResultSet findResults = findQueryExec.execSelect();

        if (findResults.hasNext()) {
            // Individual with the specified ID exists, retrieve its data
            QuerySolution solution = findResults.next();
            JsonObject jsonObject = new JsonObject();
            jsonObject.put("title", solution.get("title").toString());
            jsonObject.put("description", solution.get("description").toString());
            jsonObject.put("date", solution.get("date").toString());
            jsonObject.put("type", solution.get("type").toString());

            // Convert the JSON to a string
            String jsonResult = jsonObject.toString();
            return ResponseEntity.status(HttpStatus.OK).body(jsonResult);
        } else {
            // Individual with the specified ID doesn't exist
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event with ID " + id + " not found.");
        }
    }

}

