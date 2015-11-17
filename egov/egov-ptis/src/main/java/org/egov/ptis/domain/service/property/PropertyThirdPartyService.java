package org.egov.ptis.domain.service.property;

import static org.egov.ptis.constants.PropertyTaxConstants.APPLICATION_TYPE_ALTER_ASSESSENT;
import static org.egov.ptis.constants.PropertyTaxConstants.APPLICATION_TYPE_BIFURCATE_ASSESSENT;
import static org.egov.ptis.constants.PropertyTaxConstants.APPLICATION_TYPE_DEMOLITION;
import static org.egov.ptis.constants.PropertyTaxConstants.APPLICATION_TYPE_NEW_ASSESSENT;
import static org.egov.ptis.constants.PropertyTaxConstants.APPLICATION_TYPE_REVISION_PETITION;
import static org.egov.ptis.constants.PropertyTaxConstants.APPLICATION_TYPE_TAX_EXEMTION;
import static org.egov.ptis.constants.PropertyTaxConstants.APPLICATION_TYPE_TRANSFER_OF_OWNERSHIP;
import static org.egov.ptis.constants.PropertyTaxConstants.APPLICATION_TYPE_VACANCY_REMISSION;
import static org.egov.ptis.constants.PropertyTaxConstants.FILESTORE_MODULE_NAME;
import static org.egov.ptis.constants.PropertyTaxConstants.NOTICE_TYPE_MUTATION_CERTIFICATE;
import static org.egov.ptis.constants.PropertyTaxConstants.NOTICE_TYPE_SPECIAL_NOTICE;
import static org.egov.ptis.constants.PropertyTaxConstants.STATUS_APPROVED;
import static org.egov.ptis.constants.PropertyTaxConstants.STATUS_OPEN;
import static org.egov.ptis.constants.PropertyTaxConstants.STATUS_REJECTED;
import static org.egov.ptis.constants.PropertyTaxConstants.WFLOW_ACTION_END;
import static org.egov.ptis.constants.PropertyTaxConstants.WF_STATE_BILL_COLLECTOR_APPROVED;
import static org.egov.ptis.constants.PropertyTaxConstants.WF_STATE_CLOSED;
import static org.egov.ptis.constants.PropertyTaxConstants.WF_STATE_COMMISSIONER_APPROVED;
import static org.egov.ptis.constants.PropertyTaxConstants.WF_STATE_REJECTED;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.filestore.service.FileStoreService;
import org.egov.infra.workflow.entity.StateHistory;
import org.egov.infstr.services.PersistenceService;
import org.egov.ptis.domain.dao.property.BasicPropertyDAO;
import org.egov.ptis.domain.entity.objection.RevisionPetition;
import org.egov.ptis.domain.entity.property.PropertyImpl;
import org.egov.ptis.domain.entity.property.PropertyMutation;
import org.egov.ptis.domain.entity.property.VacancyRemission;
import org.egov.ptis.domain.service.transfer.PropertyTransferService;
import org.egov.ptis.notice.PtNotice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class PropertyThirdPartyService {

    private static final Logger LOGGER = Logger.getLogger(PropertyThirdPartyService.class);
    public PersistenceService persistenceService;

    @Autowired
    @Qualifier("fileStoreService")
    protected FileStoreService fileStoreService;

    @Autowired
    private PropertyTransferService transferOwnerService;

    @Autowired
    private BasicPropertyDAO basicPropertyDAO;

    //For Exemption and vacancyremission is in progess
    public byte[] getSpecialNotice(String assessmentNo, String applicationNo, String applicationType)
            throws IOException {
        PtNotice ptNotice = null;
        if (applicationType.equals(APPLICATION_TYPE_NEW_ASSESSENT)
                || applicationType.equals(APPLICATION_TYPE_ALTER_ASSESSENT)
                || applicationType.equals(APPLICATION_TYPE_BIFURCATE_ASSESSENT)
                || applicationType.equals(APPLICATION_TYPE_DEMOLITION)
                || applicationType.equals(APPLICATION_TYPE_TAX_EXEMTION)) {
            if (StringUtils.isNotBlank(applicationNo)) {
                ptNotice = (PtNotice) persistenceService.find(
                        "from PtNotice where applicationNumber = ? and noticeType = ?", applicationNo,
                        NOTICE_TYPE_SPECIAL_NOTICE);
            } else if (StringUtils.isNotBlank(assessmentNo)) {
                ptNotice = (PtNotice) persistenceService.find("from PtNotice where basicProperty.upicNo = ?",
                        assessmentNo);
            }
        } else if (applicationType.equals(APPLICATION_TYPE_TRANSFER_OF_OWNERSHIP)) {
            if (StringUtils.isNotBlank(applicationNo)) {
                ptNotice = (PtNotice) persistenceService.find(
                        "from PtNotice where applicationNumber = ? and noticeType = ?", applicationNo,
                        NOTICE_TYPE_MUTATION_CERTIFICATE);
            }

        }
        if (ptNotice != null && ptNotice.getFileStore() != null) {
            final FileStoreMapper fsm = ptNotice.getFileStore();
            final File file = fileStoreService.fetch(fsm, FILESTORE_MODULE_NAME);
            return FileUtils.readFileToByteArray(file);
        } else
            return null;
    }

    public Map<String, String> validatePropertyStatus(String applicationNo, String applicationType) {
        PropertyImpl property = null;
        PropertyMutation mutation = null;
        VacancyRemission vacancyRemission = null;
        RevisionPetition revisionPetition = null;
        StateHistory stateHistory = null;
        Map<String, String> statusCommentsMap = new HashMap<String, String>();
        if (applicationType.equals(APPLICATION_TYPE_NEW_ASSESSENT)
                || applicationType.equals(APPLICATION_TYPE_ALTER_ASSESSENT)
                || applicationType.equals(APPLICATION_TYPE_BIFURCATE_ASSESSENT)
                || applicationType.equals(APPLICATION_TYPE_VACANCY_REMISSION)
                || applicationType.equals(APPLICATION_TYPE_TAX_EXEMTION)) {
            if (StringUtils.isNotBlank(applicationNo)) {
                property = (PropertyImpl) persistenceService.find("From PropertyImpl where applicationNo = ? ",
                        applicationNo);
            }
            if (!property.getState().getHistory().isEmpty()) {
                int size = property.getState().getHistory().size();
                stateHistory = property.getState().getHistory().get(size - 1);
            }
            if (property.getState().getValue().equals(WF_STATE_CLOSED)
                    && stateHistory.getValue().endsWith(WF_STATE_COMMISSIONER_APPROVED)) {
                statusCommentsMap.put("status", STATUS_APPROVED);
                statusCommentsMap.put("comments", stateHistory.getComments());
                statusCommentsMap.put("updatedBy", stateHistory.getLastModifiedBy().getName());
            } else if (property.getState().getValue().endsWith(WF_STATE_COMMISSIONER_APPROVED)) {
                statusCommentsMap.put("status", STATUS_APPROVED);
                statusCommentsMap.put("comments", property.getState().getComments());
                statusCommentsMap.put("updatedBy", property.getState().getLastModifiedBy().getName());
            } else if (property.getState().getValue().equals(WF_STATE_CLOSED)
                    && stateHistory.getValue().endsWith(WF_STATE_REJECTED)) {
                statusCommentsMap.put("status", STATUS_REJECTED);
                statusCommentsMap.put("comments", property.getState().getComments());
                statusCommentsMap.put("updatedBy", property.getState().getLastModifiedBy().getName());
            } else {
                statusCommentsMap.put("status", STATUS_OPEN);
                statusCommentsMap.put("comments", property.getState().getComments());
                statusCommentsMap.put("updatedBy", property.getState().getLastModifiedBy().getName());
            }

        } else if (applicationType.equals(APPLICATION_TYPE_TRANSFER_OF_OWNERSHIP)) {
            if (StringUtils.isNotBlank(applicationNo)) {
                mutation = transferOwnerService.getPropertyMutationByApplicationNo(applicationNo);
            }
            if (!mutation.getState().getHistory().isEmpty()) {
                int size = mutation.getState().getHistory().size();
                stateHistory = mutation.getState().getHistory().get(size - 1);
            }
            if (mutation.getState().getValue().equals(WF_STATE_CLOSED)
                    && stateHistory.getValue().equals(WF_STATE_COMMISSIONER_APPROVED)) {
                statusCommentsMap.put("status", STATUS_APPROVED);
                statusCommentsMap.put("comments", stateHistory.getComments());
                statusCommentsMap.put("updatedBy", stateHistory.getLastModifiedBy().getName());
            } else if (mutation.getState().getValue().equals(WF_STATE_COMMISSIONER_APPROVED)) {
                statusCommentsMap.put("status", STATUS_APPROVED);
                statusCommentsMap.put("comments", mutation.getState().getComments());
                statusCommentsMap.put("updatedBy", mutation.getState().getLastModifiedBy().getName());
            } else if (mutation.getState().getValue().equals(WF_STATE_CLOSED)
                    && stateHistory.getValue().equals(WF_STATE_REJECTED)) {
                statusCommentsMap.put("status", STATUS_REJECTED);
                statusCommentsMap.put("comments", mutation.getState().getComments());
                statusCommentsMap.put("updatedBy", mutation.getState().getLastModifiedBy().getName());
            } else {
                statusCommentsMap.put("status", STATUS_OPEN);
                statusCommentsMap.put("comments", mutation.getState().getComments());
                statusCommentsMap.put("updatedBy", mutation.getState().getLastModifiedBy().getName());
            }
        } else if (applicationType.equals(APPLICATION_TYPE_VACANCY_REMISSION)) {
            if (StringUtils.isNotBlank(applicationNo)) {
                vacancyRemission = (VacancyRemission) persistenceService.find(
                        "From VacancyRemission where applicationNumber = ? ", applicationNo);
            }
            if (!vacancyRemission.getState().getHistory().isEmpty()) {
                int size = vacancyRemission.getState().getHistory().size();
                stateHistory = vacancyRemission.getState().getHistory().get(size - 1);
            }
            if (vacancyRemission.getState().getValue().equals(WF_STATE_CLOSED)
                    && stateHistory.getValue().endsWith(WF_STATE_BILL_COLLECTOR_APPROVED)) {
                statusCommentsMap.put("status", STATUS_APPROVED);
                statusCommentsMap.put("comments", vacancyRemission.getState().getComments());
                statusCommentsMap.put("updatedBy", vacancyRemission.getState().getLastModifiedBy().getName());
            } else if (vacancyRemission.getState().getValue().equals(WF_STATE_CLOSED)
                    && stateHistory.getValue().endsWith(WF_STATE_REJECTED)) {
                statusCommentsMap.put("status", STATUS_REJECTED);
                statusCommentsMap.put("comments", stateHistory.getComments());
                statusCommentsMap.put("updatedBy", stateHistory.getLastModifiedBy().getName());
            } else {
                statusCommentsMap.put("status", STATUS_OPEN);
                statusCommentsMap.put("comments", vacancyRemission.getState().getComments());
                statusCommentsMap.put("updatedBy", vacancyRemission.getState().getLastModifiedBy().getName());
            }
        } else if (applicationType.equals(APPLICATION_TYPE_REVISION_PETITION)) {
            if (StringUtils.isNotBlank(applicationNo)) {
                revisionPetition = (RevisionPetition) persistenceService.find(
                        "From RevisionPetition where objectionNumber = ? ", applicationNo);
            }
            if (!revisionPetition.getState().getHistory().isEmpty()) {
                int size = revisionPetition.getState().getHistory().size();
                stateHistory = revisionPetition.getState().getHistory().get(size - 1);
            }
            if ((revisionPetition.getState().getValue().equals(WFLOW_ACTION_END) || revisionPetition.getState()
                    .getValue().equals("Print Special Notice"))
                    && stateHistory.getValue().endsWith("Approved")) {
                statusCommentsMap.put("status", STATUS_APPROVED);
                statusCommentsMap.put("comments", stateHistory.getComments());
                statusCommentsMap.put("updatedBy", stateHistory.getLastModifiedBy().getName());
            } else if (revisionPetition.getState().getValue().endsWith("Approved")) {
                statusCommentsMap.put("status", STATUS_APPROVED);
                statusCommentsMap.put("comments", revisionPetition.getState().getComments());
                statusCommentsMap.put("updatedBy", revisionPetition.getState().getLastModifiedBy().getName());
            } else if (revisionPetition.getState().getValue().equals(WFLOW_ACTION_END)) {
                statusCommentsMap.put("status", STATUS_REJECTED);
                statusCommentsMap.put("comments", stateHistory.getComments());
                statusCommentsMap.put("updatedBy", stateHistory.getLastModifiedBy().getName());
            } else {
                statusCommentsMap.put("status", STATUS_OPEN);
                statusCommentsMap.put("comments", revisionPetition.getState().getComments());
                statusCommentsMap.put("updatedBy", revisionPetition.getState().getLastModifiedBy().getName());
            }
        }
        return statusCommentsMap;
    }

    public PersistenceService getPersistenceService() {
        return persistenceService;
    }

    public void setPersistenceService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

}
