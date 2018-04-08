package com.yafred.asn1.parser;

import java.io.IOException;
import java.io.InputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import com.yafred.asn1.grammar.ASNLexer;
import com.yafred.asn1.grammar.ASNParser;
import com.yafred.asn1.model.Specification;
import com.yafred.asn1.parser.SpecificationAntlrVisitor;

public class Asn1ModelBuilder {
	
	public static void main(String[] args) throws IOException {
		if(args.length == 0) {
			System.err.println("Needs an argument (path to a file in the filesystem or resource in the classpath). "
					+ "For example: com/yafred/asn1/test/unit/a_000.asn");
			System.exit(1);
		}
		
		new Asn1ModelBuilder().build(args[0]);
	}

	public void build(String resourceName) throws IOException {
		
        System.out.println("=================  BUILD ====================================================================");
		System.out.println(resourceName);

		// load test data (needs parser/target/test-classes in path
        InputStream inStream = getClass().getClassLoader().getResourceAsStream(resourceName);

        CharStream charStream = null;
        if (inStream != null) {
        	charStream = CharStreams.fromStream(inStream);
        }
        else {
        	charStream = CharStreams.fromFileName(resourceName);
        }

        ASNLexer lexer = new ASNLexer(charStream);
        TokenStream tokens = new CommonTokenStream(lexer);
        ASNParser parser = new ASNParser(tokens);
        ParseTree tree = parser.specification();
        
        SpecificationAntlrVisitor visitor = new SpecificationAntlrVisitor();
        Specification specification = visitor.visit(tree);
        
        if(inStream != null) {
	        inStream.close();
	        inStream = getClass().getClassLoader().getResourceAsStream(resourceName);  // reload
	
	        System.out.println("-----------------  TEST DATA  ---------------------------------------------------------------");
	        
	        System.out.println(convertStreamToString(inStream));
        }
        
        System.out.println("-----------------  DUMP MODEL ---------------------------------------------------------------");
          
        new Asn1SpecificationWriter(System.out).visit(specification);

        System.out.println("-----------------  VALIDATE MODEL -----------------------------------------------------------");
        
        Asn1ModelValidator asn1ModelValidator = new Asn1ModelValidator();
        asn1ModelValidator.visit(specification);
        for(String error : asn1ModelValidator.getWarningList()) {
        	System.out.println(error);
        }
        for(String error : asn1ModelValidator.getErrorList()) {
        	System.err.println(error);
        }
        
        System.out.println("-----------------  VALIDATED MODEL INFO -----------------------------------------------------");
        asn1ModelValidator.dump();
        
        System.out.println("-----------------  DUMP MODEL AGAIN ---------------------------------------------------------");
        
        new Asn1SpecificationWriter(System.out).visit(specification);

	}

	static String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is);
	    s.useDelimiter("\\A");
	    String ret = s.hasNext() ? s.next() : "";
	    s.close();
	    return ret;
	}

}