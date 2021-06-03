package de.gwdg.metadataqa.marc.ws;

import de.gwdg.metadataqa.marc.cli.Validator;
import de.gwdg.metadataqa.marc.cli.parameters.ValidatorParameters;
import de.gwdg.metadataqa.marc.cli.utils.RecordIterator;
import de.gwdg.metadataqa.marc.definition.DataSource;
import org.apache.commons.cli.ParseException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * QA Catalogueweb service
 */
@SpringBootApplication
@RestController
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @GetMapping("/hello")
    public String sayHello(
        @RequestParam(value = "myName", defaultValue = "World") String name
    ) {
        return String.format("Hello %s!", name);
    }

    @PostMapping("/validate")
    public String validate(

      @RequestParam(value = "marcVersion", defaultValue = "MARC21", required = false) String marcVersion,
      @RequestParam(value = "marcFormat", defaultValue = "XML", required = false) String marcFormat,
      @RequestParam(value = "details", defaultValue = "true", required = false) boolean details,
      @RequestParam(value = "trimId", defaultValue = "true", required = false) boolean trimId,
      @RequestParam(value = "summary", defaultValue = "true", required = false) boolean summary,
      @RequestParam(value = "outputFormat", defaultValue = "csv", required = false) String format,
      @RequestParam(value = "defaultRecordType", defaultValue = "BOOKS", required = false) String defaultRecordType,
      @RequestParam(value = "detailsFileName", defaultValue = "issue-details.csv", required = false) String detailsFileName,
      @RequestParam(value = "summaryFileName", defaultValue = "issue-summary.csv", required = false) String summaryFileName,
      @RequestParam("file") MultipartFile file
    ) throws ParseException, IOException {
        ValidatorParameters params = new ValidatorParameters();
        params.setMarcVersion(marcVersion);
        params.setMarcFormat(marcFormat);
        params.setDoDetails(details);
        params.setTrimId(trimId);
        params.setDoSummary(summary);
        params.setFormat(format);
        params.setDefaultRecordType(defaultRecordType);
        params.setDetailsFileName(detailsFileName);
        params.setSummaryFileName(summaryFileName);

        params.setDataSource(DataSource.STREAM);
        params.setStream(file.getInputStream());

        Validator validator = new Validator(params);
        RecordIterator iterator = new RecordIterator(validator);
        iterator.start();
        return String.format("validate!");
    }
}
