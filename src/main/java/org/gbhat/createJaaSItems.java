package org.gbhat;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import models.jaasitems.CpsFlowDefinition;
import models.jaasitems.Definition;
import models.jaasitems.Folder;
import models.jaasitems.Items;
import models.jaasitems.Pipeline;

@Command(name = "createJaaSItems", mixinStandardHelpOptions = true, version = "createJaaSItems 0.1",
    description = "createJaaSItems made with jbang")
class createJaaSItems implements Callable<Integer> {
    final YAMLFactory factory = new YAMLFactory()
        .enable(YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR)
        .enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE)
        .disable(YAMLGenerator.Feature.SPLIT_LINES)
        .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);

    private ObjectMapper mapper = new ObjectMapper(factory);

    @Option(names = "--folder-count", description = "Number of folders", required = true)
    private int folderCount;

    @Option(names = "--jobs-per-folder", description = "Number of jobs per folder", required = true)
    private int jobsPerFolderCount;

    @Option(names = "--pipeline-script", description = "Pipeline script", required = true)
    private String pipelineScriptName;

    @Option(names = "--output-filename", description = "Output filename", defaultValue = "items")
    private String outputFilename;

    public static void main(String... args) {
        int exitCode = new CommandLine(new createJaaSItems()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        String pipelineScript = getPipelineScript();

        for(int i=0;i<folderCount;i++) {
            Items items = new Items();
            Folder folder = new Folder();
            folder.setKind("folder");
            String paddedFolderName = "folder-" + String.format("%04d", i);
            folder.setName(paddedFolderName);
            folder.setDisplayName(paddedFolderName);
            for(int j=0;j<jobsPerFolderCount;j++) {
                CpsFlowDefinition cpsFlowDefinition = new CpsFlowDefinition();
                cpsFlowDefinition.setScript(pipelineScript);

                Definition definition = new Definition();
                definition.setCpsFlowDefinition(cpsFlowDefinition);

                Pipeline pipeline = new Pipeline();
                pipeline.setKind("pipeline");
                String paddedPipelineName = "job-" + String.format("%04d", j);
                pipeline.setName(paddedPipelineName);
                pipeline.setDisplayName(paddedPipelineName);
                pipeline.setDefinition(definition);

                folder.addItem(pipeline);
            }
            items.addItem(folder);
            writeYamlFile(items,i);
        }
        return 0;
    }

    private String getPipelineScript() throws Exception {
        return new String(Files.readAllBytes(Paths.get(pipelineScriptName)));
    }

    private void writeYamlFile(Items items,int count) throws Exception {
        String realFilename = outputFilename + "-" + String.format("%04d", count) + ".yaml";
        mapper.writeValue(new File(realFilename), items);
    }
}
