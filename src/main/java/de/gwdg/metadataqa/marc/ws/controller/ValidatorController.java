package de.gwdg.metadataqa.marc.ws.controller;

import de.gwdg.metadataqa.marc.cli.Validator;
import de.gwdg.metadataqa.marc.cli.parameters.ValidatorParameters;
import de.gwdg.metadataqa.marc.cli.utils.RecordIterator;
import de.gwdg.metadataqa.marc.definition.DataSource;
import de.gwdg.metadataqa.marc.model.validation.ValidationError;
import de.gwdg.metadataqa.marc.model.validation.ValidationErrorFormatter;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

@Controller
public class ValidatorController {

  private static final Logger logger = Logger.getLogger(Validator.class.getCanonicalName());

  @GetMapping("/")
  public String form(
    @RequestParam(value = "marcVersion", defaultValue = "MARC21", required = false) String marcVersion,
    @RequestParam(value = "marcFormat", defaultValue = "XML", required = false) String marcFormat,
    @RequestParam(value = "details", required = false) boolean details,
    @RequestParam(value = "trimId", required = false) boolean trimId,
    @RequestParam(value = "summary", required = false) boolean summary,
    @RequestParam(value = "outputFormat", defaultValue = "csv", required = false) String format,
    @RequestParam(value = "defaultRecordType", defaultValue = "BOOKS", required = false) String defaultRecordType,
    @RequestParam(value = "detailsFileName", defaultValue = "issue-details.csv", required = false) String detailsFileName,
    @RequestParam(value = "summaryFileName", defaultValue = "issue-summary.csv", required = false) String summaryFileName,
    Model model
    ) {
    logger.info("FROM: summary; " + summary);
    model.addAttribute("marcVersion", marcVersion);
    model.addAttribute("marcFormat", marcFormat);
    model.addAttribute("details", details);
    model.addAttribute("trimId", trimId);
    model.addAttribute("summary", summary);
    model.addAttribute("format", format);
    model.addAttribute("defaultRecordType", defaultRecordType);
    model.addAttribute("detailsFileName", detailsFileName);
    model.addAttribute("summaryFileName", summaryFileName);

    return "form";
  }

  @PostMapping("/evaluate")
  public String validateHtml(
    @RequestParam(value = "marcVersion", defaultValue = "MARC21", required = false) String marcVersion,
    @RequestParam(value = "marcFormat", defaultValue = "XML", required = false) String marcFormat,
    @RequestParam(value = "details", required = false) boolean details,
    @RequestParam(value = "trimId", required = false) boolean trimId,
    @RequestParam(value = "summary", required = false) boolean summary,
    @RequestParam(value = "outputFormat", defaultValue = "csv", required = false) String format,
    @RequestParam(value = "defaultRecordType", defaultValue = "BOOKS", required = false) String defaultRecordType,
    @RequestParam(value = "detailsFileName", defaultValue = "issue-details.csv", required = false) String detailsFileName,
    @RequestParam(value = "summaryFileName", defaultValue = "issue-summary.csv", required = false) String summaryFileName,
    @RequestParam(value = "content", defaultValue = "", required = false) String content,
    @RequestParam("file") MultipartFile file,
    Model model
  ) {
    logger.info("EVALUATE: summary; " + summary);
    logger.info("/evaluate");
    model.addAttribute("marcVersion", marcVersion);
    model.addAttribute("marcFormat", marcFormat);
    model.addAttribute("details", details);
    model.addAttribute("trimId", trimId);
    model.addAttribute("summary", summary);
    model.addAttribute("format", format);
    model.addAttribute("defaultRecordType", defaultRecordType);
    model.addAttribute("detailsFileName", detailsFileName);
    model.addAttribute("summaryFileName", summaryFileName);

    try {
      validate(marcVersion, marcFormat, details, trimId, summary, "html", defaultRecordType, detailsFileName, summaryFileName, content, file, model);
    } catch (ParseException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    logger.info("/evaluate after validate");
    logger.info("size: " + ((List<ValidationError>) model.getAttribute("errors")).size());

    return "result";
  }

  @PostMapping("/validate")
  public ResponseEntity<String> validateRest(
    @RequestParam(value = "marcVersion", defaultValue = "MARC21", required = false) String marcVersion,
    @RequestParam(value = "marcFormat", defaultValue = "XML", required = false) String marcFormat,
    @RequestParam(value = "details", defaultValue = "true", required = false) boolean details,
    @RequestParam(value = "trimId", defaultValue = "true", required = false) boolean trimId,
    @RequestParam(value = "summary", defaultValue = "true", required = false) boolean summary,
    @RequestParam(value = "outputFormat", defaultValue = "csv", required = false) String format,
    @RequestParam(value = "defaultRecordType", defaultValue = "BOOKS", required = false) String defaultRecordType,
    @RequestParam(value = "detailsFileName", defaultValue = "issue-details.csv", required = false) String detailsFileName,
    @RequestParam(value = "summaryFileName", defaultValue = "issue-summary.csv", required = false) String summaryFileName,
    @RequestParam(value = "content", defaultValue = "", required = false) String content,
    @RequestParam("file") MultipartFile file,
    Model model
  ) throws ParseException, IOException {
    logger.info("validate");
    validate(marcVersion, marcFormat, details, trimId, summary, format, defaultRecordType, detailsFileName, summaryFileName, content, file, model);
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.set("Baeldung-Example-Header", "Value-ResponseEntityBuilderWithHttpHeaders");
    responseHeaders.set("Content-Type", (String) model.getAttribute("contentType"));

    return ResponseEntity.ok()
      .contentType(MediaType.valueOf((String) model.getAttribute("contentType")))
      .headers(responseHeaders)
      .body((String) model.getAttribute("result"));
  }

  private void validate(String marcVersion,
                        String marcFormat,
                        boolean details,
                        boolean trimId,
                        boolean summary,
                        String format,
                        String defaultRecordType,
                        String detailsFileName,
                        String summaryFileName,
                        String content,
                        MultipartFile file,
                        Model model
  ) throws ParseException, IOException {
    logger.info("validate()");
    ValidatorParameters params = new ValidatorParameters();
    params.setMarcVersion(marcVersion);
    logger.info("setMarcFormat: " + marcFormat);
    params.setMarcFormat(marcFormat);
    logger.info("getMarcFormat: " + params.getMarcFormat());
    params.setDoDetails(details);
    params.setTrimId(trimId);
    params.setDoSummary(summary);
    if (format.equals("html"))
      params.setFormat("json");
    else
      params.setFormat(format);
    params.setDefaultRecordType(defaultRecordType);
    params.setDetailsFileName(detailsFileName);
    params.setSummaryFileName(summaryFileName);
    params.setCollectAllErrors(true);
    params.setOutputDir("/tmp/ws");
    params.setLimit(100);

    params.setDataSource(DataSource.STREAM);
    InputStream stream = null;
    if (file != null)
      stream = file.getInputStream();
    else if (StringUtils.isNotBlank(content))
      stream = new ByteArrayInputStream(content.getBytes());

    if (stream != null)
      params.setStream(file.getInputStream());

    Validator validator = new Validator(params);
    RecordIterator iterator = new RecordIterator(validator);
    iterator.start();

    StringBuffer sb = new StringBuffer();
    model.addAttribute("numberOfRecords", validator.getCounter());
    model.addAttribute("numberOfProcessedRecords", validator.getNumberOfprocessedRecords());

    // sb.append(String.format("validated! counted: %d, processed records: %d\n", validator.getCounter(), validator.getNumberOfprocessedRecords()));
    List<ValidationError> errors = validator.getAllValidationErrors();
    logger.info("errors: " + errors.size());
    // sb.append(String.format("number of errors: %d\n", errors.size()));
    if (errors.size() > 0) {
      String header = ValidationErrorFormatter.formatHeaderForCollector(params.getFormat());
      if (!header.equals(""))
        sb.append(header + "\n");

      for (ValidationError error : errors) {
        String formatted = ValidationErrorFormatter.format(error, params.getFormat());
        // System.err.println(formatted);
        sb.append(formatted + "\n");
      }
    }

    logger.info("format: " + format);
    String contentType = "text/html";
    if (format.equals("html")) {
      model.addAttribute("errors", errors);
    } else {
      if (format.equals("json"))
        contentType = "application/json";
      else if (format.equals("csv"))
        contentType = "text/csv";
      else if (format.equals("txt"))
        contentType = "text/plain";
      model.addAttribute("contentType", contentType);
      model.addAttribute("result", sb.toString());
    }
  }
}
