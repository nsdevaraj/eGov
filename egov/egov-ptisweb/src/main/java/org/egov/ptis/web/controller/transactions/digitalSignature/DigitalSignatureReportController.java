package org.egov.ptis.web.controller.transactions.digitalSignature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.egov.infra.security.utils.SecurityUtils;
import org.egov.infra.workflow.entity.StateAware;
import org.egov.infra.workflow.entity.WorkflowTypes;
import org.egov.infra.workflow.inbox.InboxRenderServiceDeligate;
import org.egov.ptis.constants.PropertyTaxConstants;
import org.egov.ptis.service.transactions.DigitalSignatureReportService;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/digitalSignature")
public class DigitalSignatureReportController {

    private static final String DIGITAL_SIGNATURE_REPORT_FORM = "digitalSignatureReport-form";

    @Autowired
    private DigitalSignatureReportService digitalSignatureReportService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private InboxRenderServiceDeligate<StateAware> inboxRenderServiceDeligate;

    private Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    @RequestMapping(value = "/digitalSignatureReport-form", method = RequestMethod.GET)
    public String searchForm(final Model model) {
        final List<HashMap<String, Object>> resultList = getRecordsForDigitalSignature();
        model.addAttribute("digitalSignatureReportList", resultList);
        return DIGITAL_SIGNATURE_REPORT_FORM;
    }

    public List<HashMap<String, Object>> getRecordsForDigitalSignature() {
        final List<HashMap<String, Object>> resultList = new ArrayList<HashMap<String, Object>>();
        final List<StateAware> stateAwareList = fetchItems();
        if (null != stateAwareList && !stateAwareList.isEmpty()) {
            HashMap<String, Object> tempMap = new HashMap<String, Object>();
            WorkflowTypes workflowTypes = null;
            List<WorkflowTypes> workflowTypesList = new ArrayList<WorkflowTypes>();
            for (final StateAware record : stateAwareList)
                if (record != null)
                    if (record.getState() != null &&
                    record.getState().getNextAction().equalsIgnoreCase(PropertyTaxConstants.DIGITAL_SIGNATURE_PENDING)) {
                        tempMap = new HashMap<String, Object>();
                        workflowTypesList = getCurrentSession().getNamedQuery(WorkflowTypes.WF_TYPE_BY_TYPE_AND_RENDER_Y)
                                .setString(0, record.getStateType()).list();
                        if (workflowTypesList != null && !workflowTypesList.isEmpty())
                            workflowTypes = workflowTypesList.get(0);
                        else
                            workflowTypes = null;
                        tempMap.put("objectId", record.getId());
                        tempMap.put("type", workflowTypes != null ? workflowTypes.getDisplayName() : null);
                        tempMap.put("module", workflowTypes != null ? workflowTypes.getModule().getDisplayName() : null);
                        tempMap.put("details", record.getStateDetails());
                        resultList.add(tempMap);
                    }
        }

        return resultList;
    }

    public List<StateAware> fetchItems() {
        final List<StateAware> digitalSignWFItems = new ArrayList<StateAware>();
        digitalSignWFItems.addAll(inboxRenderServiceDeligate.getInboxItems(securityUtils.getCurrentUser().getId()));
        return digitalSignWFItems;
    }

}
