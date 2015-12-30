import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Variable;
import org.semanticweb.yars.nx.namespace.OWL;
import org.semanticweb.yars.nx.namespace.RDF;

import edu.kit.aifb.datafu.Binding;
import edu.kit.aifb.datafu.Origin;
import edu.kit.aifb.datafu.Program;
import edu.kit.aifb.datafu.Request;
import edu.kit.aifb.datafu.Request.Method;
import edu.kit.aifb.datafu.RequestRule;
import edu.kit.aifb.datafu.SelectQuery;
import edu.kit.aifb.datafu.consumer.impl.BindingConsumerCollection;
import edu.kit.aifb.datafu.engine.EvaluateProgram;
import edu.kit.aifb.datafu.io.mediatypes.MediaTypes;
import edu.kit.aifb.datafu.io.origins.FileOrigin;
import edu.kit.aifb.datafu.io.origins.InternalOrigin;
import edu.kit.aifb.datafu.io.origins.RequestOrigin;
import edu.kit.aifb.datafu.io.serialisers.NQuadsSerialiser;
import edu.kit.aifb.datafu.io.sinks.BindingConsumerSink;
import edu.kit.aifb.datafu.io.sinks.FileSink;
import edu.kit.aifb.datafu.parser.ProgramConsumer;
import edu.kit.aifb.datafu.parser.ProgramConsumerImpl;
import edu.kit.aifb.datafu.parser.QueryConsumerImpl;
import edu.kit.aifb.datafu.parser.notation3.Notation3Parser;
import edu.kit.aifb.datafu.parser.notation3.ParseException;
import edu.kit.aifb.datafu.parser.sparql.SparqlParser;
import edu.kit.aifb.datafu.planning.EvaluateProgramConfig;
import edu.kit.aifb.datafu.planning.EvaluateProgramGenerator;
import edu.kit.aifb.datafu.utils.Config.Compression;

public class index {

	public static void main(String[] args) throws ParseException, IOException, InterruptedException, URISyntaxException, edu.kit.aifb.datafu.parser.sparql.ParseException {
		System.out.println("start");
/*
 * 	Generate a Program Object
 */
 
		File file = new File("/Users/raphaelmanke/Documents/linked-data-fu-0.9.8/examples/chefkoch.n3");
		//	InputStream pis = new FileInputStream(file);
		//	Origin pbase = new FileOrigin(file.toURI());
		InputStream pis = new FileInputStream(file);
		
		Origin pbase = new FileOrigin(file,null, null);
		

		// 	Notation3Parser n3p = new Notation3Parser(pis);
		//	ProgramConsumer pc = new ProgramConsumer(pbase.getResource());
		Notation3Parser n3p = new Notation3Parser(pis);
		//ProgramConsumer pc = new Progra
		ProgramConsumer pc = new ProgramConsumerImpl(pbase); //Error: Cannont Instatiate ProgramConsumer. With 9.9.8 that worked.

		// 	n3p.parse(pc);
		n3p.parse(pc, pbase);
		
		// 	pis.close();
		pis.close();
		
		Program program = new Program(pbase);
		
/*
 * Query		
 */
		String query = new String (""
				+ "PREFIX arecipe: <http://purl.org/amicroformat/arecipe/> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "SELECT DISTINCT ?s ?rezeptName "
				+ "WHERE {"
				+ "?s rdf:identifier \"searchResult\" ;"
				+ "rdf:name ?rezeptName."
				+ "}"
				);
/*
 *  Register a Query		
 */
		
		QueryConsumerImpl qc = new QueryConsumerImpl(pbase);		
		SparqlParser sp = new SparqlParser(new StringReader(query));
		sp.parse(qc, new InternalOrigin("SparqlSelectTest"));
		SelectQuery sq = qc.getSelectQueries().iterator().next();

		
		//BindingConsumerPrint bcp = new BindingConsumerPrint(out);
		BindingConsumerCollection bc = new BindingConsumerCollection(); //Error: BindingConsumerSet cannot resolved to a type
		//BindingConsumerPrint bp = new BindingConsumerPrint();
		
		//BindingConsumerSink sink = new BindingConsumerSink(bc);
		
		FileSink sink = new FileSink(new File("/Users/raphaelmanke/Downloads/linked-data-fu-0.9.9/out.srx"), Compression.None); 
		sink.setMediaType(MediaTypes.SparqlResultsNx); 

		System.out.println(sink.toString().toString());
		program.registerSelectQuery(sq, sink); //Error: die Methode addSelectQuery is not defined.
/*
 * Create an Derivation Rule
 */
		
		Set<Nodes> pattern = Collections.singleton(new Nodes(new Variable("x"), OWL.SAMEAS, new Variable("y"))); 
		Request r = new Request(new BNode("reqid"), Method.GET, new Variable("y")); 
		RequestRule rr = new RequestRule(new BNode("trid"), r, pattern); 

		program.addRequestRule(rr); 
/*
 * 	Create an EvaluateProgram Object
 */
		EvaluateProgramConfig config = new EvaluateProgramConfig();
		EvaluateProgramGenerator ep = new EvaluateProgramGenerator(program, config);
		EvaluateProgram epg = ep.getEvaluateProgram();
	
/*
 * 	Evaluate the Program
 */
		
		epg.start();
		URI uri = new URI("http://manke-hosting.de/wrapper/index.php/explore/apfelkuchen");
		RequestOrigin requestOrigin = new RequestOrigin(uri, Method.GET);
		epg.getInputOriginConsumer().consume(requestOrigin);
		
		URI uri1 = new URI("http://wrapper:8888/index.php/reweSuche/butter");
		RequestOrigin requestOrigin1 = new RequestOrigin(uri1, Method.GET);
		epg.getInputOriginConsumer().consume(requestOrigin1);
		
		System.out.println("Consumed");
		
		

		
		
		epg.awaitIdleAndFinish();
		

		
		
		
		System.out.println(epg.getEvaluateOutputOrigin().toString());
		epg.shutdown();
	}

}
