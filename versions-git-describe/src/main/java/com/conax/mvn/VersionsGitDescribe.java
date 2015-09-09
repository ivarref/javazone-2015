package com.conax.mvn;

import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.*;
import org.apache.maven.shared.invoker.*;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.jetbrains.annotations.NotNull;
import pl.project13.maven.git.GitDataProvider;
import pl.project13.maven.git.GitDescribeConfig;
import pl.project13.maven.git.GitDirLocator;
import pl.project13.maven.git.JGitProvider;
import pl.project13.maven.git.log.MavenLoggerBridge;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

@Component(role = EventSpy.class, hint = VersionsGitDescribe.HINT)
public class VersionsGitDescribe implements EventSpy {
    public static final String HINT = "versionsGitDescribe";

    @Requirement
    private Logger logger;

    @Requirement
    private ProjectBuilder projectBuilder;

    private File pom;
    private boolean doRelease = false;

    @Override
    public void init(Context context) throws Exception {
    }

    @Override
    public void onEvent(Object event) throws Exception {
        try {
            if (event instanceof MavenExecutionRequest) {
                MavenExecutionRequest execRequest = (MavenExecutionRequest) event;
                doRelease = "true".equals(execRequest.getUserProperties().getProperty("release", "false"));
                if (doRelease) {
                    setReleaseVersions(execRequest);
                } else {
                    logger.info("VersionsGitDescribe :: Keeping versions ...");
                }
            } else if (event instanceof MavenExecutionResult && doRelease) {
                revertVersions();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }


    private void setVersions(File projectPom, String version) throws MavenInvocationException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(projectPom);
        request.setGoals(Collections.singletonList("versions:set"));
        Properties pc = request.getProperties() != null ? request.getProperties() : new Properties();
        pc.put("newVersion", version);
        request.setProperties(pc);

        Invoker invoker = new DefaultInvoker();
        invoker.setOutputHandler(silentOutputHandler());
        invoker.execute(request);
    }

    private void setReleaseVersions(MavenExecutionRequest mavenExecutionRequest) throws MavenInvocationException, ProjectBuildingException, IOException, MojoExecutionException {
        ProjectBuildingRequest buildingRequest = mavenExecutionRequest.getProjectBuildingRequest();
        ProjectBuildingResult buildingResult = projectBuilder.build(mavenExecutionRequest.getPom(), buildingRequest);
        MavenProject project = buildingResult.getProject();
        String version = getGitDescribe(project);

        logger.info("VersionsGitDescribe :: Setting version to '" + version + "' ...");

        pom = mavenExecutionRequest.getPom();
        setVersions(pom, version);

        // change all modules because mvn versions:set will actually break on some modules
        for (String module : project.getModules()) {
            File modulePom = new File(new File(pom.getParentFile(), module), "pom.xml");
            File backup = new File(modulePom.getParentFile(), "pom.xml.versionsBackup");
            if (!modulePom.exists()) {
                String error = "Could not find module pom file " + modulePom.getAbsolutePath();
                throw new RuntimeException(error);
            } else if (modulePom.exists() && backup.exists()) {
            } else if (modulePom.exists() && !backup.exists()) {
                logger.info("VersionsGitDescribe :: changing " + modulePom);
                setVersions(modulePom, version);
            } else {
                throw new RuntimeException("Unknown state");
            }
        }
        logger.info("VersionsGitDescribe :: Setting version to '" + version + "' ... OK!");
    }

    private void revertVersions() throws MavenInvocationException {
        logger.info("VersionsGitDescribe :: reverting versions ...");
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(pom);
        request.setGoals(Collections.singletonList("versions:revert"));
        Invoker invoker = new DefaultInvoker();
        invoker.setOutputHandler(silentOutputHandler());
        invoker.execute(request);
        logger.info("VersionsGitDescribe :: reverting versions ... OK");
    }

    private String getGitDescribe(MavenProject project) throws IOException, MojoExecutionException {
        File gitDir = new GitDirLocator(project, Arrays.<MavenProject>asList()).lookupGitDirectory(new File(project.getBasedir(), ".git"));
        GitDataProvider gitDataProvider = JGitProvider.on(gitDir, new MavenLoggerBridge(getLogger(), true))
                .setVerbose(false)
                .setAbbrevLength(7)
                .setPrefixDot("git.")
                .setDateFormat("dd.MM.yyyy '@' HH:mm:ss z")
                .setGitDescribe(new GitDescribeConfig());
        Properties gitProps = new Properties();
        gitDataProvider.loadGitData(gitProps);
        return gitProps.getProperty("git.commit.id.describe");
    }

    private static Log getLogger() {
        return new SystemStreamLog();
    }

    @NotNull
    private InvocationOutputHandler silentOutputHandler() {
        return new InvocationOutputHandler() {
            @Override
            public void consumeLine(String s) {

            }
        };
    }

    @Override
    public void close() throws Exception {
    }

}