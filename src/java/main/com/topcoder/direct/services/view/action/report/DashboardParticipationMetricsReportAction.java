/*
 * Copyright (C) 2010 - 2012 TopCoder Inc., All Rights Reserved.
 */
package com.topcoder.direct.services.view.action.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.topcoder.direct.services.view.dto.dashboard.participationreport.ParticipationAggregationReportDTO;
import com.topcoder.direct.services.view.dto.dashboard.participationreport.ParticipationBasicReportDTO;
import com.topcoder.direct.services.view.dto.dashboard.participationreport.ParticipationReportDTO;
import com.topcoder.direct.services.view.form.DashboardReportForm;
import com.topcoder.direct.services.view.util.DataProvider;
import com.topcoder.direct.services.view.util.DirectUtils;

/**
 * <p>A <code>Struts</code> action to be used for handling the requests for viewing the
 * Participation Metrics Report.</p>
 *
 * <p>
 * Version 1.1 (TC Cockpit Permission and Report Update One) change log:
 * <ol>
 *   <li>This class has been refactoring. It's extended from <code>DashboardReportBaseAction</code>. The base class will
 *   parse and validate the parameters, prepare for the common data used by the report page.</li>
 *   <li>Updated method <code></code>aggregateParticipationDetails(List, ParticipationAggregationType)</code> to 
 *   aggregation the unique winners, total final winners and total milestone winners data.</li>
 *   <li>Updated method {@link #executeAction()()} to pass <code>TCSubject</code> object to method
 *     <code>DataProvided.getDashboardParticipationReport</code> which will use <code>TCSubject</code> to
 *     check the user's permission.</li>
 * </ol>
 * </p>
 *
 * <p>
 * Version 1.2 (Release Assembly - TC Cockpit - Member Participation Metrics Report Performance Enhancement Assembly) 
 * Change notes:
 *   <ol>
 *     <li>Re-worked the {@link #executeAction()} method to delegate complete report data calculation to 
 *     {@link DataProvider} class.</li>
 *   </ol>
 * </p>
 * 
 * @author TCSASSEMBER
 * @version 1.2 (TC Cockpit Participation Metrics Report Part One Assembly 1)
 */
public class DashboardParticipationMetricsReportAction extends DashboardReportBaseAction<DashboardReportForm, ParticipationReportDTO> {
    
    /**
     * Represents the serial version unique id.
     */
    private static final long serialVersionUID = 3386028267770976602L;
    
    /**
     * <p>Constructs new <code>DashboardParticipationMetricsReportAction</code> instance.</p>
     */
    public DashboardParticipationMetricsReportAction() {
        super();
        
        setViewData(new ParticipationReportDTO());
        setFormData(new DashboardReportForm());
    }
    
    /**
     * <p>Handles the incoming request.
     *
     * @return <code>SUCCESS</code> if request is to be forwarded to respective view or <code>download</code> if
     *         response is to be written to client directly.
     */
    @Override
    public String execute() throws Exception {
        String result = super.execute();
        if (SUCCESS.equals(result)) {
            if (getFormData().isExcel()) {
                return "download";
            }
        }
        return result;
    }
    
    /**
     * <p>Handles the incoming request. Retrieves data for Participation Metrics Report
     * and binds it to request.</p>
     *
     * @throws Exception if an unexpected error occurs.
     */
    @Override
    protected void executeAction() throws Exception {
        super.executeAction();
        
        if (hasActionErrors()) {
            return;
        }
        
        // Analyze form parameters
        DashboardReportForm form = getFormData();
        long projectId = form.getProjectId();
        long[] categoryIds = form.getProjectCategoryIds();
        long customerId = form.getCustomerId();
        long billingAccountId = form.getBillingAccountId();
        long[] statusIds = form.getStatusIds();
        String[] statusNames = new String[statusIds.length];
        for (int i = 0; i < statusIds.length; i++) {
            long statusId = statusIds[i];
            String statusName = REPORT_CONTEST_STATUS.get(statusId);
            statusNames[i] = statusName;
        }
        Date startDate = DirectUtils.getDate(form.getStartDate());
        Date endDate = DirectUtils.getDate(form.getEndDate());
        
        // If necessary get and process report data
        if (!getViewData().isShowJustForm()) {
            // Create empty DTOs to collect report data  
            ParticipationBasicReportDTO basicMetrics = new ParticipationBasicReportDTO();
            List<ParticipationAggregationReportDTO> billingAggregation 
                = new ArrayList<ParticipationAggregationReportDTO>();
            List<ParticipationAggregationReportDTO> projectAggregation
                = new ArrayList<ParticipationAggregationReportDTO>();
            List<ParticipationAggregationReportDTO> contestTypeAggregation
                = new ArrayList<ParticipationAggregationReportDTO>();
            List<ParticipationAggregationReportDTO> statusAggregation
                = new ArrayList<ParticipationAggregationReportDTO>();

            // Query for report data
            DataProvider.getDashboardParticipationReport(
                getCurrentUser(), projectId, categoryIds, customerId, billingAccountId, statusNames, startDate, endDate,
                basicMetrics, billingAggregation, projectAggregation, contestTypeAggregation, statusAggregation);

            // Set view data with report data
            getViewData().setParticipationBasicReport(basicMetrics);
            getViewData().setBillingAggregation(billingAggregation);
            getViewData().setProjectAggregation(projectAggregation);
            getViewData().setContestTypeAggregation(contestTypeAggregation);
            getViewData().setStatusAggregation(statusAggregation);
        }
    }
}
