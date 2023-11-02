package com.example.socialmediawebsemantique;

import com.example.socialmediawebsemantique.tools.jenaEngine;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
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

@RestController
public class ReclamationController {

    Model model = jenaEngine.readModel("data/oneZero.owl");

    //Reclamation

    @GetMapping("/ReplyReclamSearch")
    @CrossOrigin(origins = "*")
    public String getReplyReclams(
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


            String queryStr = FileManager.get().readWholeFileAsUTF8("data/query_ReplyReclam.txt");


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
                jsonObject.put("ReplyReclam", solution.get("ReplyReclam").toString());
                jsonObject.put("title", solution.get("title").toString());
                jsonObject.put("description", solution.get("description").toString());
                jsonObject.put("date", solution.get("date").toString());

                jsonArray.add(jsonObject);
            }


        }

        String jsonResult = jsonArray.toString();

        System.out.println(jsonResult);
        return jsonResult;




    }


    @GetMapping("/ReclamationSearch")
    @CrossOrigin(origins = "*")
    public String getReclamations(
            @RequestParam(value = "domain", required = false) String domain
    ) {
        String NS = "";
        
        JsonArray jsonArray = null;
        if (model != null) {
            NS = model.getNsPrefixURI("");

            // apply our rules on the owlInferencedModel
            Model inferedModel = jenaEngine.readInferencedModelFromRuleFile(model, "data/rules.txt");

            // query on the model after inference
            /*OutputStream res =  JenaEngine.executeQueryFile(inferedModel, "data/query_Skill.txt");

            System.out.println(res);
            return res.toString();*/

            String queryStr = FileManager.get().readWholeFileAsUTF8("data/query_Reclamation.txt");



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
            // Exécuter la requête pour les événements en ligne
            Query query = QueryFactory.create(queryStr);
            QueryExecution qexec = QueryExecutionFactory.create(query, inferedModel);
            ResultSet results = qexec.execSelect();

// Créer un tableau JSON pour stocker les résultats de la requête sur les événements en ligne
            jsonArray = new JsonArray();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                JsonObject jsonObject = new JsonObject();
                jsonObject.put("Reclamation", solution.get("Reclamation").toString());
                jsonObject.put("title", solution.get("title").toString());
                jsonObject.put("description", solution.get("description").toString());
                String dateValue = solution.get("date").toString();
                String cleanedDate = dateValue.replace("^^http://www.w3.org/2001/XMLSchema#dateTime", "");

                jsonObject.put("date", cleanedDate);
                jsonArray.add(jsonObject);
            }



// Convertir le JSON en une chaîne
            String jsonResult = jsonArray.toString();

            System.out.println(jsonResult);
            return jsonResult;

        }

        return null;

    }



    //Delete Reclamation
    // Supprimer un post par title en utilisant une requête SPARQL
    @DeleteMapping("/deleteReclamation")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> deleteReclamation(@RequestParam("title") String title) {
        // Charger les données RDF depuis un fichier
        Model model = jenaEngine.readModel("data/oneZero.owl");

        // Créer un modèle Ont qui effectue des inférences
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, model);

        // Construire la requête SPARQL pour supprimer l'individu par ID
        String sparqlDeleteQuery = "PREFIX ns: <http://www.semanticweb.org/sadok/ontologies/2023/9/untitled-ontology-9#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "DELETE {\n" +
                "  ?Reclamation rdf:type ns:Reclamation;\n" +
                "         ns:title ?title;\n" +
                "         ns:date ?date;\n" +
                "         ns:description ?description;\n" +
                "} WHERE {\n" +
                "  ?Reclamation rdf:type ns:Reclamation;\n" +
                "         ns:title ?title;\n" +
                "         ns:date ?date;\n" +
                "         ns:description ?description;\n" +
                "FILTER (?title = \"" + title + "\")\n" +
                "}\n";

        System.out.println(sparqlDeleteQuery);



        // Exécuter la requête SPARQL pour supprimer l'individu
        UpdateAction.parseExecute(sparqlDeleteQuery, ontModel);

        // Enregistrer le modèle RDF mis à jour
        try (OutputStream outputStream = Files.newOutputStream(Paths.get("data/oneZero.owl"))) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Échec de la suppression de la Post.");
        }

        return ResponseEntity.status(HttpStatus.OK).body("Post supprimé avec succès.");
    }




    //Delete ReplyReclamation
    // Supprimer un post par title en utilisant une requête SPARQL
    @DeleteMapping("/deleteReplyReclamation")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> deleteReplyReclamation(@RequestParam("title") String title) {
        // Charger les données RDF depuis un fichier
        Model model = jenaEngine.readModel("data/oneZero.owl");

        // Créer un modèle Ont qui effectue des inférences
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, model);

        // Construire la requête SPARQL pour supprimer l'individu par ID
        String sparqlDeleteQuery = "PREFIX ns: <http://www.semanticweb.org/sadok/ontologies/2023/9/untitled-ontology-9#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "DELETE {\n" +
                "  ?ReplyReclam rdf:type ns:ReplyReclam;\n" +
                "         ns:title ?title;\n" +
                "         ns:date ?date;\n" +
                "         ns:description ?description;\n" +
                "} WHERE {\n" +
                "  ?ReplyReclam rdf:type ns:ReplyReclam;\n" +
                "         ns:title ?title;\n" +
                "         ns:date ?date;\n" +
                "         ns:description ?description;\n" +
                "FILTER (?title = \"" + title + "\")\n" +
                "}\n";

        System.out.println(sparqlDeleteQuery);



        // Exécuter la requête SPARQL pour supprimer l'individu
        UpdateAction.parseExecute(sparqlDeleteQuery, ontModel);

        // Enregistrer le modèle RDF mis à jour
        try (OutputStream outputStream = Files.newOutputStream(Paths.get("data/oneZero.owl"))) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Échec de la suppression de la Post.");
        }

        return ResponseEntity.status(HttpStatus.OK).body("Post supprimé avec succès.");
    }



    //Create Post

    @PostMapping("/createReclamation")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> createEvent(@RequestBody ReclamationModel reclamationRequest) throws ParseException {
        // Extract data from the EventRequest object
        Integer id = reclamationRequest.getId();
        String title = reclamationRequest.getTitle();
        String description = reclamationRequest.getDescription();
        String dateString = reclamationRequest.getDate();

        // Load RDF data from a file
        Model model = jenaEngine.readModel("data/oneZero.owl");

        // Create an OntModel for inferencing with the correct namespace
        String NS = "http://www.semanticweb.org/sadok/ontologies/2023/9/untitled-ontology-9#";
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, model);

        // Parse the date from the string
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = dateFormat.parse(dateString);

        // Create the RDF properties using the correct URIs
        String reclamationURI = NS + "Reclamation_" + generateUniqueID();
        String dateURI = NS + "date";
        String titleURI = NS + "title";
        String idURI = NS + "id";
        String descriptionURI = NS + "description";

        // Create an individual for the new event with the appropriate URI
        Individual newReclamation = ontModel.createIndividual(reclamationURI, ontModel.createClass(NS + "Reclamation"));

        // Set the properties of the event using the correct URIs
        newReclamation.addProperty(ontModel.getProperty(idURI), ontModel.createTypedLiteral(id, XSDDatatype.XSDinteger));
        newReclamation.addProperty(ontModel.getProperty(titleURI), title);
        newReclamation.addProperty(ontModel.getProperty(descriptionURI), description);
        newReclamation.addProperty(ontModel.getProperty(dateURI), ontModel.createTypedLiteral(date, XSDDatatype.XSDdateTime));

        // Save the updated RDF model
        try (OutputStream outputStream = Files.newOutputStream(Paths.get("data/oneZero.owl"))) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create the reclamation.");
        }

        return ResponseEntity.status(HttpStatus.OK).body("Reclamation created successfully.");
    }

    // Generate a unique ID for the new event
    private String generateUniqueID() {
        return String.valueOf(System.currentTimeMillis());
    }





}
