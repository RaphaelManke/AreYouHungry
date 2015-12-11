import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Variable;

import com.sun.research.ws.wadl.Resource;

import edu.kit.aifb.datafu.Origin;
import edu.kit.aifb.datafu.Program;
import edu.kit.aifb.datafu.Request.Method;
import edu.kit.aifb.datafu.SelectQuery;
import edu.kit.aifb.datafu.consumer.OriginConsumer;
import edu.kit.aifb.datafu.engine.EvaluateProgram;
import edu.kit.aifb.datafu.io.origins.FileOrigin;
import edu.kit.aifb.datafu.io.origins.FileOrigin.Mode;
import edu.kit.aifb.datafu.io.origins.InternalOrigin;
import edu.kit.aifb.datafu.io.origins.RequestOrigin;
import edu.kit.aifb.datafu.io.serialisers.NQuadsSerialiser;
import edu.kit.aifb.datafu.io.sink.EvaluateSink;
import edu.kit.aifb.datafu.io.sinks.BindingConsumerSink;
import edu.kit.aifb.datafu.parser.ProgramConsumer;
import edu.kit.aifb.datafu.parser.notation3.Notation3Parser;
import edu.kit.aifb.datafu.parser.notation3.ParseException;
import edu.kit.aifb.datafu.parser.sparql.SparqlParser;
import edu.kit.aifb.datafu.planning.EvaluateProgramConfig;
import edu.kit.aifb.datafu.planning.EvaluateProgramGenerator;

public class index {

	public static void main(String[] args) throws ParseException, IOException, InterruptedException, URISyntaxException {
		System.out.println("start");
/*
 * 	Generate a Program Object
 */
 
		File file = new File("/Users/raphaelmanke/Documents/linked-data-fu-0.9.8/examples/chefkoch.n3");
		//	InputStream pis = new FileInputStream(file);
		//	Origin pbase = new FileOrigin(file.toURI());
		InputStream pis = new FileInputStream(file);
		
		Origin pbase = new FileOrigin(file, null, null);
		

		// 	Notation3Parser n3p = new Notation3Parser(pis);
		//	ProgramConsumer pc = new ProgramConsumer(pbase.getResource());
		Notation3Parser n3p = new Notation3Parser(pis);
		ProgramConsumer pc = new ProgramConsumer(pbase);

		// 	n3p.parse(pc);
		n3p.parse(pc, pbase);
		
		// 	pis.close();
		pis.close();
		
		Program program = new Program(pbase);
/*
 *  Register a Query		
 */
		

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
		System.out.println("Consumed");
		Object obj = epg.getOutputOriginConsumer();
		//NQuadsSerialiser ser = new NQuadsSerialiser(os);
		epg.getOutputOriginConsumer();

		epg.awaitIdleAndFinish();
		
		System.out.println(epg.getEvaluateOutputOrigin().toString());
		epg.shutdown();
	}

}
