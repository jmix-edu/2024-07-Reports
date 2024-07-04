package com.company.jmixpmflowbase.app;

import com.company.jmixpmflowbase.entity.Project;
import com.company.jmixpmflowbase.entity.ProjectStatus;
import com.company.jmixpmflowbase.entity.User;
import io.jmix.core.DataManager;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.runner.ReportRunner;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

@Component
public class ReportServiceBean {

    private final ReportRunner reportRunner;
    private final FileStorage fileStorage;
    private final DataManager dataManager;

    public ReportServiceBean(ReportRunner reportRunner, FileStorage fileStorage, DataManager dataManager) {
        this.reportRunner = reportRunner;
        this.fileStorage = fileStorage;
        this.dataManager = dataManager;
    }

    public void generateSingleUserReport(User user) {
        final ReportOutputDocument document = reportRunner.byReportCode("user-report")
                .withParams(Map.of("entity", user))
                .withOutputType(ReportOutputType.DOCX)
                .withOutputNamePattern("user-report.docx")
                .run();
        final byte[] reportContent = document.getContent();
        final String reportName = document.getDocumentName();

        FileRef fileRef = fileStorage.saveStream(reportName, new ByteArrayInputStream(reportContent));
        user.setDocument(fileRef);
        dataManager.save(user);
    }

    public List<Map<String, Object>> getProjectsList(ProjectStatus status) {
        List<Project> projectList = dataManager.load(Project.class)
                .query("select p from Project p where p.status = :status")
                .parameter("status", status)
                .list();

        return projectList.stream()
                .map(project -> Map.<String, Object>of(
                        "name", project.getName() == null ? "" : project.getName(),
                        "status", project.getStatus() == null ? "" : project.getStatus(),
                        "manager", project.getManager() == null ? "" : project.getManager().getDisplayName())
                ).toList();
    }
}