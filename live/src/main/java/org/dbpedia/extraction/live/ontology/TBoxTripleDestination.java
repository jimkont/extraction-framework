package org.dbpedia.extraction.live.ontology;

import com.hp.hpl.jena.rdf.model.*;
import org.apache.commons.collections15.MultiMap;
import org.apache.log4j.Logger;
import org.dbpedia.extraction.live.util.sparql.ISparulExecutor;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;

/**
 * User: Dimitris Kontokostas
 * Description
 * Created: 5/9/14 12:10 PM
 */
public class TBoxTripleDestination
{
    private static Logger logger = Logger.getLogger(TBoxTripleDestination.class);

    private String					reifierPrefix;
    private String					dataGraphName;
    private String					metaGraphName;

    private ISparulExecutor executor;
    // private ISparulExecutor dataExecutor;
    // private ISparulExecutor metaExecutor;
    private MessageDigest md5;

    private static Resource origin =
            //ResourceFactory.createResource(MyVocabulary.NS + TBoxExtractor.class.getSimpleName());
            ResourceFactory.createResource(MyVocabulary.NS + "TBoxExtractor");


    public TBoxTripleDestination(ISparulExecutor executor,
                                 String dataGraphName, String metaGraphName, String refifierPrefix)
            throws NoSuchAlgorithmException
    {
        this.executor = executor;
        this.dataGraphName = dataGraphName;
        this.metaGraphName = metaGraphName;

        this.reifierPrefix = refifierPrefix;

        md5 = MessageDigest.getInstance("MD5");
    }



    private void deleteFromMetaGraphBySourcePage(Resource sourcePage)
            throws Exception
    {
        String query = DBpediaQLUtil.deleteMetaBySourcePage(
                sourcePage.toString(), metaGraphName);
			/*
			"Delete From <" + metaGraphName + ">\n" +
			"{\n" +
				"?b ?x ?y\n" +
			"}\n" +
			"From <" + metaGraphName + "> \n" +
			"{\n" +
				"?b <" + MyVocabulary.DBM_SOURCE_PAGE + "> <" + sourcePage + ">  .\n" +
				"?b ?x ?y .\n" +
			"}\n";
			*/
        logger.debug("Running query: 'deleteFromMetaGraphBySourcePage'");
        executor.executeUpdate(query);
    }

    private void deleteFromDataGraphBySourcePage(Resource sourcePage)
            throws Exception
    {
        String query = DBpediaQLUtil.deleteDataBySourcePage(
                sourcePage.toString(), dataGraphName, metaGraphName);
			/*
			"Delete From <" + dataGraphName + ">\n" +
			"{\n" +
				"?s ?p ?o\n" +
			"}\n" +
			"From <" + metaGraphName + "> {\n" +
				"?b <" + MyVocabulary.DBM_SOURCE_PAGE + "> <" + sourcePage + "> .\n" +
				"?b <" + MyVocabulary.OWL_ANNOTATED_SOURCE + "> ?s .\n" +
				"?b <" + MyVocabulary.OWL_ANNOTATED_PROPERTY + "> ?p .\n" +
				"?b <" + MyVocabulary.OWL_ANNOTATED_TARGET + "> ?o .\n" +
			"}\n";
*/
        logger.debug("Running query: 'deleteFromDataGraphBySourcePage'");
        executor.executeUpdate(query);
    }

    /**
     *
     * @param oaiId
     * @throws Exception
     */
    private void deleteFromDataGraph(Resource oaiId)
            throws Exception
    {
        String query =
                "Delete From <" + dataGraphName + ">\n" +
                        "{\n" +
                        "?s ?p ?o\n" +
                        "}\n" +
                        "{\n" +
                        "Graph <" + dataGraphName + "> {\n" +
                        "?a <" + MyVocabulary.DBM_OAIIDENTIFIER + "> <" + oaiId + ">\n" +
                        "}\n" +
                        "Graph <" + metaGraphName + "> {\n" +
                        "?b <" + MyVocabulary.DBM_SOURCE_PAGE + "> ?a .\n" +
                        "?b <" + MyVocabulary.OWL_ANNOTATED_SOURCE + "> ?s .\n" +
                        "?b <" + MyVocabulary.OWL_ANNOTATED_PROPERTY + "> ?p .\n" +
                        "?b <" + MyVocabulary.OWL_ANNOTATED_TARGET + "> ?o .\n" +
                        "}\n" +
                        "}\n";


        logger.debug("Running query: 'deleteFromDataGraph'");
        executor.executeUpdate(query);
    }

    private void deleteFromMetaGraph(Resource oaiId)
            throws Exception
    {
        String query =
                "Delete From <" + metaGraphName + ">\n" +
                        "{\n" +
                        "?b ?x ?y\n" +
                        "}\n" +
                        "From <" + metaGraphName + "> \n" +
                        "{\n" +
                        "?t <" + MyVocabulary.OWL_ANNOTATED_PROPERTY + "> <" + MyVocabulary.DBM_OAIIDENTIFIER + "> .\n" +
                        "?t <" + MyVocabulary.OWL_ANNOTATED_TARGET + "> <" + oaiId + "> .\n" +

                        "?t <" + MyVocabulary.DBM_SOURCE_PAGE + "> ?a .\n" +
                        "?b <" + MyVocabulary.DBM_SOURCE_PAGE + "> ?a .\n" +
                        "?b ?x ?y .\n" +
                        "}\n";


        logger.debug("Running query: 'deleteFromMetaGraph'");
        executor.executeUpdate(query);
    }

    private Model reify(Model result, Statement triple, Resource reifier)
    {
        result.add(reifier, MyVocabulary.OWL_ANNOTATED_SOURCE, triple.getSubject());
        result.add(reifier, MyVocabulary.OWL_ANNOTATED_PROPERTY, triple.getPredicate());
        result.add(reifier, MyVocabulary.OWL_ANNOTATED_TARGET, triple.getObject());

        return result;
    }

    private void insertIntoDataGraph(Resource rootId, Resource sourcePage,
                                     MultiMap<Resource, Model> triples)
            throws Exception
    {
        /*
		Date date = new Date();
		Format formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		String dateString = formatter.format(date);
		int split = dateString.length() - 2;
		dateString = dateString.substring(0, split) + ":" + dateString.substring(split);
		)*/



        //RDFResourceNode dcModified = new RDFResourceNode(
        //		MyVocabulary.DC_MODIFIED.getIRI());

        //IRI xsdDateTime = IRI.create(XSD.dateTime.getURI());

        Model inserts = ModelFactory.createDefaultModel();


        for (Map.Entry<Resource, Collection<Model>> item : triples
                .entrySet()) {

            for (Model tmp : item.getValue()) {
                inserts.add(tmp);
            }
        }

        // RDFResourceNode oaiIdPredicate = new
        // RDFResourceNode(MyVocabulary.DBM_OAIIDENTIFIER.getUri());

        // IRI sourcePage, IRI oaiId,
		/*
		 * inserts.add( new RDFTriple(new RDFResourceNode(sourcePage),
		 * oaiIdPredicate, new RDFResourceNode(oaiId)));
		 */
        logger.debug("Running query: 'insertIntoDataGraph'");
        executor.insert(inserts, dataGraphName);
    }

    private Resource generateMD5HashUri(String pageId, Statement triple)
    {
        return ResourceFactory.createResource(reifierPrefix + pageId + "_" + generateMD5(triple));
    }

    private String generateMD5(Statement triple)
    {
        String str = triple.getSubject().toString() + " "
                + triple.getPredicate().toString() + " "
                + triple.getObject().toString();

        return generateMD5(str);
    }

    private String generateMD5(String str)
    {
        md5.reset();
        md5.update(str.getBytes());
        byte[] result = md5.digest();

        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            hexString.append(Integer.toHexString(0xFF & result[i]));
        }
        return hexString.toString();
    }

    private void insertIntoMetaGraph(Resource rootIRI, Resource sourcePage, String pageId,
                                     MultiMap<Resource, Model> triples)
            throws Exception
    {
		/*
		Date date = new Date();
		Format formatter = new SimpleDateFormat("yyyy.MM.dd'T'HH:mm:ss");
		String dateString = formatter.format(date);

		RDFResourceNode dcModified = new RDFResourceNode(
				MyVocabulary.DC_MODIFIED.getIRI());
		IRI xsdDateTime = IRI.create(XSD.dateTime.getURI());*/
        /*
		RDFResourceNode predicateSourcePage = new RDFResourceNode(
				MyVocabulary.DBM_SOURCE_PAGE.getIRI());
		RDFResourceNode objectSourcePage = new RDFResourceNode(rootIRI);
		RDFResourceNode aspectPredicate = new RDFResourceNode(
				MyVocabulary.DBM_ASPECT.getIRI());

		RDFResourceNode originPredicate = new RDFResourceNode(
				MyVocabulary.DBM_EXTRACTED_BY.getIRI());
		*/

        //Set<RDFTriple> inserts = new HashSet<RDFTriple>();
        Model inserts = ModelFactory.createDefaultModel();
        for (Map.Entry<Resource, Collection<Model>> item : triples
                .entrySet()) {
            Resource aspect = item.getKey();

            for (Model tmp : item.getValue()) {
                for (Statement triple : tmp.listStatements().toSet()) {
                    Resource reifier = generateMD5HashUri(pageId, triple);

                    reify(inserts, triple, reifier);

					/*
					inserts.add(new RDFTriple(reifier, dcModified,
							new RDFLiteralNode(dateString, xsdDateTime)));
					*/
                    inserts.add(reifier, MyVocabulary.DBM_SOURCE_PAGE, rootIRI);
                    inserts.add(reifier, MyVocabulary.DBM_ORIGIN, origin);

                    if (aspect != null) {
                        inserts.add(reifier, MyVocabulary.DBM_ASPECT, aspect);
                    }
                }
            }
        }

        logger.debug("Running query: 'insertIntoMetaGraph'");
        executor.insert(inserts, metaGraphName);
    }

    // ttlp insert
	/*
	private void insert(String graphName, Set<RDFTriple> triples)
		throws Exception
	{
		executor.insert(triples, graphName);
	}
	 */
	/*
	private void insert(String graphName, Set<RDFTriple> triples)
		throws Exception
	{
		String query = "Insert into <" + graphName + "> {\n";

		for (RDFTriple triple : triples)
			query += SparqlHelper.toSparqlString(triple) + " .\n";

		query += "}\n";

		executor.executeUpdate(query);
		//System.out.println(graphName);
		//System.out.println(query);
	}
	 */
    public void delete(Resource oaiId)
            throws Exception
    {
        deleteFromDataGraph(oaiId);
        deleteFromMetaGraph(oaiId);
    }

    public void update(Resource rootIRI, Resource sourcePage, Resource oaiId, String pageId,
                       MultiMap<Resource, Model> triples)
            throws Exception
    {
        deleteFromDataGraphBySourcePage(rootIRI);
        deleteFromMetaGraphBySourcePage(rootIRI);

        if(triples != null) {
            insertIntoMetaGraph(rootIRI, sourcePage, pageId, triples);
            insertIntoDataGraph(rootIRI, sourcePage, triples);
        }
    }

}

