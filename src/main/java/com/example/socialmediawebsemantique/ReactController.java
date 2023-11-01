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
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.util.FileManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.StreamSupport;


import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@RestController
public class ReactController {
    Model model = jenaEngine.readModel("data/oneZero.owl");
    @GetMapping("/LikeReactSearch")
    @CrossOrigin(origins = "*")
    public String getLikeReact(
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


            String queryStr = FileManager.get().readWholeFileAsUTF8("data/query_Like.txt");


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
                jsonObject.put("Like", solution.get("Like").toString());
                jsonObject.put("title", solution.get("title").toString());
                jsonObject.put("nombrelike", solution.get("nombrelike").toString());
                jsonObject.put("nombredislike", solution.get("nombredislike").toString());
                jsonObject.put("date", solution.get("date").toString());

                jsonArray.add(jsonObject);
            }


        }

        // Convert the JSON to a string
        String jsonResult = jsonArray.toString();

        System.out.println(jsonResult);
        return jsonResult;




    }
    @GetMapping("/ReactSearch")
    @CrossOrigin(origins = "*")
    public String getReacts(
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
            /*OutputStream res =  JenaEngine.executeQueryFile(inferedModel, "data/query_Skill.txt");

            System.out.println(res);
            return res.toString();*/

            String queryStr = FileManager.get().readWholeFileAsUTF8("data/query_React.txt");



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
                jsonObject.put("React", solution.get("React").toString());
                jsonObject.put("title", solution.get("title").toString());
                // Obtenir la valeur du nombre de likes et de dislikes
                RDFNode nombreLikeNode = solution.get("nombrelike");
                if (nombreLikeNode.isLiteral()) {
                    String nombreLikeValue = nombreLikeNode.asLiteral().getString();
                    jsonObject.put("nombrelike", nombreLikeValue);
                }

                RDFNode nombreDislikeNode = solution.get("nombredislike");
                if (nombreDislikeNode.isLiteral()) {
                    String nombreDislikeValue = nombreDislikeNode.asLiteral().getString();
                    jsonObject.put("nombredislike", nombreDislikeValue);
                }
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



    @GetMapping("/ReactSearchSortedByLikes")
    @CrossOrigin(origins = "*")
    public String getReactsSortedByLikes(
            @RequestParam(value = "domain", required = false) String domain
    ) {
        String NS = "";
        // lire le modèle à partir d'une ontologie
        JsonArray jsonArray = null;
        if (model != null) {
            // lire le Namespace de l'ontologie
            NS = model.getNsPrefixURI("");

            // appliquer nos règles sur le modèle avec inférence
            Model inferredModel = jenaEngine.readInferencedModelFromRuleFile(model, "data/rules.txt");

            // requête sur le modèle après inférence
            String queryStr = FileManager.get().readWholeFileAsUTF8("data/query_React.txt");

            // définir la valeur de ?domainParam
            if (domain != null && !domain.isEmpty()) {
                // Remplacez le paramètre par la valeur de domaine réelle
                queryStr = queryStr.replace("?titleParam", '\"' + domain + '\"');
            } else {
                // Si le domaine n'est pas fourni, supprimez le paramètre et la condition FILTER de la requête
                queryStr = queryStr.replace("FILTER (?titleParam != \"\" && ?title = ?titleParam)", "");
            }

            System.out.println(queryStr);
            // Exécutez la requête
            Query query = QueryFactory.create(queryStr);
            QueryExecution qexec = QueryExecutionFactory.create(query, inferredModel);

            // Exécutez la requête
            ResultSet results = qexec.execSelect();

            // Transformez les résultats en une liste

            List<QuerySolution> resultSolutions = new ArrayList<>();
            while (results.hasNext()) {
                resultSolutions.add(results.next());
            }

            // Triez la liste des résultats par le nombre de likes (le nombre de likes le plus élevé en premier)
            resultSolutions.sort((sol1, sol2) -> {
                RDFNode likes1 = sol1.get("nombrelike");
                RDFNode likes2 = sol2.get("nombrelike");

                if (likes1.isLiteral() && likes2.isLiteral()) {
                    int likeValue1 = likes1.asLiteral().getInt();
                    int likeValue2 = likes2.asLiteral().getInt();
                    return Integer.compare(likeValue2, likeValue1);
                } else {
                    return 0; // Gestion des cas non littéraux
                }
            });

            jsonArray = new JsonArray();
            for (QuerySolution solution : resultSolutions) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.put("React", solution.get("React").toString());
                jsonObject.put("title", solution.get("title").toString());
                jsonObject.put("nombrelike", solution.get("nombrelike").toString());
                jsonObject.put("nombredislike", solution.get("nombredislike").toString());
                jsonObject.put("date", solution.get("date").toString());
                jsonArray.add(jsonObject);
            }
        }

        // Convertissez le JSON en une chaîne
        String jsonResult = jsonArray.toString();

        System.out.println(jsonResult);
        return jsonResult;


    }


    // Supprimer une reaction par ID en utilisant une requête SPARQL
   /* @DeleteMapping("/deleteReact")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> deleteReact(@RequestParam("id") Integer id) {
        // Charger les données RDF depuis un fichier
        Model model = jenaEngine.readModel("data/oneZero.owl");

        // Créer un modèle Ont qui effectue des inférences
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, model);

        // Construire la requête SPARQL pour supprimer l'individu par ID
        String sparqlDeleteQuery = "PREFIX ns: <http://www.semanticweb.org/sadok/ontologies/2023/9/untitled-ontology-9#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "DELETE {\n" +
                "?React rdf:type ns:React;\n" +
                "ns:id ?id;\n" +
                "ns:title ?title;\n" +
                "ns:date ?date;\n" +
                "ns:nombrelike ?nombrelike;\n" +
                "ns:nombredislike ?nombredislike;\n" +
                "} WHERE {\n" +
                "  ?React rdf:type ns:React;\n" +
                "         ns:id ?id;\n" +
                "ns:title ?title;\n" +
                "ns:date ?date;\n" +
                "ns:nombrelike ?nombrelike;\n" +
                "ns:nombredislike ?nombredislike;\n" +
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

        return ResponseEntity.status(HttpStatus.OK).body("Reaction supprimé avec succès.");
    }*/


    //Delete Post
    // Supprimer un post par title en utilisant une requête SPARQL
    @DeleteMapping("/deleteReact")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> deleteReact(@RequestParam("title") String title) {
        // Charger les données RDF depuis un fichier
        Model model = jenaEngine.readModel("data/oneZero.owl");

        // Créer un modèle Ont qui effectue des inférences
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, model);

        // Construire la requête SPARQL pour supprimer l'individu par ID
        String sparqlDeleteQuery = "PREFIX ns: <http://www.semanticweb.org/sadok/ontologies/2023/9/untitled-ontology-9#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "DELETE {\n" +
                "  ?React rdf:type ns:React;\n" +
                "         ns:id ?id;\n" +
                "         ns:title ?title;\n" +
                "         ns:nombrelike ?nombrelike;\n" +
                "         ns:nombredislike ?nombredislike;\n" +
                "         ns:date ?date;\n" +
                "} WHERE {\n" +
                "  ?React rdf:type ns:React;\n" +
                "         ns:id ?id;\n" +
                "         ns:title ?title;\n" +
                "         ns:nombrelike ?nombrelike;\n" +
                "         ns:nombredislike ?nombredislike;\n" +
                "         ns:date ?date;\n" +
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Échec de la suppression de la Reaction.");
        }

        return ResponseEntity.status(HttpStatus.OK).body("React supprimé avec succès.");
    }


    @PostMapping("/createReact")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> createReact(@RequestBody ReactModel reactRequest) throws ParseException {
        // Extract data from the EventRequest object
        Integer id = reactRequest.getId();
        String title = reactRequest.getTitle();
        Integer nombrelike = reactRequest.getNombrelike();
        Integer nombredislike = reactRequest.getNombredislike();
        String dateString = reactRequest.getDate();

        // Load RDF data from a file
        Model model = jenaEngine.readModel("data/oneZero.owl");

        // Create an OntModel for inferencing with the correct namespace
        String NS = "http://www.semanticweb.org/sadok/ontologies/2023/9/untitled-ontology-9#";
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, model);

        // Parse the date from the string
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = dateFormat.parse(dateString);

        // Create the RDF properties using the correct URIs
        String eventURI = NS + "React_" + generateUniqueID();
        String dateURI = NS + "date";
        String nombrelikeURI = NS + "nombrelike";
        String titleURI = NS + "title";
        String idURI = NS + "id";
        String nombredislikeURI = NS + "nombredislike";

        // Create an individual for the new event with the appropriate URI
        Individual newReact = ontModel.createIndividual(eventURI, ontModel.createClass(NS + "React"));

        // Set the properties of the event using the correct URIs
        newReact.addProperty(ontModel.getProperty(idURI), ontModel.createTypedLiteral(id, XSDDatatype.XSDinteger));
        newReact.addProperty(ontModel.getProperty(titleURI), title);
        newReact.addProperty(ontModel.getProperty(nombrelikeURI),  ontModel.createTypedLiteral(nombrelike, XSDDatatype.XSDinteger));
        newReact.addProperty(ontModel.getProperty(dateURI), ontModel.createTypedLiteral(date, XSDDatatype.XSDdateTime));
        newReact.addProperty(ontModel.getProperty(nombredislikeURI),  ontModel.createTypedLiteral(nombredislike, XSDDatatype.XSDinteger));

        // Save the updated RDF model
        try (OutputStream outputStream = Files.newOutputStream(Paths.get("data/oneZero.owl"))) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create the react.");
        }

        return ResponseEntity.status(HttpStatus.OK).body("React created successfully.");
    }

    // Generate a unique ID for the new event
    private String generateUniqueID() {
        return String.valueOf(System.currentTimeMillis());
    }





}
