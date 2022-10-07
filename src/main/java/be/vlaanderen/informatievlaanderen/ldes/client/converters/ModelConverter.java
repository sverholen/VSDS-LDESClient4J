package be.vlaanderen.informatievlaanderen.ldes.client.converters;

import java.io.StringWriter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParserBuilder;

import be.vlaanderen.informatievlaanderen.ldes.client.LdesClientDefaults;

public class ModelConverter {

	private ModelConverter() {
	}

	public static Model convertStringToModel(String input) {
		return convertStringToModel(input, LangConverter.convertToLang(LdesClientDefaults.DEFAULT_DATA_SOURCE_FORMAT));
	}

	public static Model convertStringToModel(String input, Lang dataSourceFormat) {
		return RDFParserBuilder.create()
				.fromString(input)
				.lang(dataSourceFormat)
				.toModel();
	}

	public static String convertModelToString(Model model) {
		return convertModelToString(model,
				LangConverter.convertToLang(LdesClientDefaults.DEFAULT_DATA_DESTINATION_FORMAT));
	}

	public static String convertModelToString(Model model, Lang dataDestinationFormat) {
		StringWriter out = new StringWriter();

		RDFDataMgr.write(out, model, dataDestinationFormat);

		return out.toString();
	}
}
