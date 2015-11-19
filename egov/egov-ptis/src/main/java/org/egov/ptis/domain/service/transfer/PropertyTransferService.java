/*    eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2015>  eGovernments Foundation
 *
 *     The updated version of eGov suite of products as by eGovernments Foundation
 *     is available at http://www.egovernments.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or
 *     http://www.gnu.org/licenses/gpl.html .
 *
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 *
 * 	1) All versions of this program, verbatim or modified must carry this
 * 	   Legal Notice.
 *
 * 	2) Any misrepresentation of the origin of the material is prohibited. It
 * 	   is required that all modified versions of this material be marked in
 * 	   reasonable ways as different from the original version.
 *
 * 	3) This license does not grant any rights to any user of the program
 * 	   with regards to rights under trademark law for use of the trade names
 * 	   or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org
 ******************************************************************************/
package org.egov.ptis.domain.service.transfer;

import static org.egov.ptis.constants.PropertyTaxConstants.APPLICATION_TYPE_TRANSFER_OF_OWNERSHIP;

import static org.egov.ptis.constants.PropertyTaxConstants.FILESTORE_MODULE_NAME;
import static org.egov.ptis.constants.PropertyTaxConstants.NOTICE_TYPE_MUTATION_CERTIFICATE;
import static org.egov.ptis.constants.PropertyTaxConstants.STATUS_ISACTIVE;
import static org.egov.ptis.constants.PropertyTaxConstants.TRANSFER;
import static org.egov.ptis.constants.PropertyTaxConstants.WFLOW_ACTION_STEP_PREVIEW;
import static org.egov.ptis.constants.PropertyTaxConstants.WFLOW_ACTION_STEP_SIGN;
import static org.egov.ptis.constants.PropertyTaxConstants.WF_STATE_CLOSED;
import static org.egov.ptis.constants.PropertyTaxConstants.NOTICE_TYPE_MUTATION_CERTIFICATE;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.http.impl.client.NoopUserTokenHandler;
import org.egov.demand.utils.DemandConstants;
import org.egov.infra.admin.master.entity.User;
import org.egov.infra.admin.master.service.CityService;
import org.egov.infra.admin.master.service.UserService;
import org.egov.infra.exception.ApplicationRuntimeException;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.filestore.service.FileStoreService;
import org.egov.infra.persistence.entity.enums.UserType;
import org.egov.infra.reporting.engine.ReportConstants.FileFormat;
import org.egov.infra.reporting.engine.ReportOutput;
import org.egov.infra.reporting.engine.ReportRequest;
import org.egov.infra.reporting.engine.ReportService;
import org.egov.infra.reporting.viewer.ReportViewerUtil;
import org.egov.infra.rest.client.SimpleRestClient;
import org.egov.infra.script.service.ScriptService;
import org.egov.infra.security.utils.SecurityUtils;
import org.egov.infra.utils.ApplicationNumberGenerator;
import org.egov.infra.utils.EgovThreadLocals;
import org.egov.infra.validation.exception.ValidationError;
import org.egov.infra.validation.exception.ValidationException;
import org.egov.infstr.services.PersistenceService;
import org.egov.pims.commons.Designation;
import org.egov.portal.entity.Citizen;
import org.egov.ptis.client.bill.PTBillServiceImpl;
import org.egov.ptis.client.util.PropertyTaxNumberGenerator;
import org.egov.ptis.client.util.PropertyTaxUtil;
import org.egov.ptis.constants.PropertyTaxConstants;
import org.egov.ptis.domain.bill.PropertyTaxBillable;
import org.egov.ptis.domain.dao.property.BasicPropertyDAO;
import org.egov.ptis.domain.dao.property.PropertyMutationMasterDAO;
import org.egov.ptis.domain.entity.enums.TransactionType;
import org.egov.ptis.domain.entity.property.BasicProperty;
import org.egov.ptis.domain.entity.property.BasicPropertyImpl;
import org.egov.ptis.domain.entity.property.Document;
import org.egov.ptis.domain.entity.property.DocumentType;
import org.egov.ptis.domain.entity.property.PropertyImpl;
import org.egov.ptis.domain.entity.property.PropertyMutation;
import org.egov.ptis.domain.entity.property.PropertyMutationMaster;
import org.egov.ptis.domain.entity.property.PropertyOwnerInfo;
import org.egov.ptis.domain.entity.property.PropertySource;
import org.egov.ptis.domain.entity.property.PtApplicationType;
import org.egov.ptis.domain.service.notice.NoticeService;
import org.egov.ptis.domain.service.property.PropertyService;
import org.egov.ptis.notice.PtNotice;
import org.egov.ptis.report.bean.PropertyAckNoticeInfo;
import org.hibernate.FlushMode;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

public class PropertyTransferService {

    @Autowired
    @Qualifier("propertyMutationService")
    private PersistenceService<PropertyMutation, Long> propertyMutationService;

    @Autowired
    @Qualifier("propertyImplService")
    private PersistenceService<PropertyImpl, Long> propertyImplService;

    @Autowired
    @Qualifier("basicPropertyService")
    private PersistenceService<BasicProperty, Long> basicPropertyService;

    @Autowired
    private BasicPropertyDAO basicPropertyDAO;

    @Autowired
    private PropertyMutationMasterDAO propertyMutationMasterDAO;

    @Autowired
    @Qualifier("fileStoreService")
    private FileStoreService fileStoreService;

    @Autowired
    @Qualifier("propertyTaxNumberGenerator")
    private PropertyTaxNumberGenerator propertyTaxNumberGenerator;

    @Autowired
    @Qualifier("documentTypePersistenceService")
    private PersistenceService<DocumentType, Long> documentTypePersistenceService;

    @Autowired
    @Qualifier("ptaxApplicationTypeService")
    private PersistenceService<PtApplicationType, Long> ptaxApplicationTypeService;

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private SimpleRestClient simpleRestClient;

    @Autowired
    private ApplicationNumberGenerator applicationNumberGenerator;

    @Autowired
    private ReportService reportService;

    @Autowired
    private ScriptService scriptService;

    @Autowired
    private PTBillServiceImpl ptBillServiceImpl;

    @Autowired
    private PropertyTaxUtil propertyTaxUtil;

    @Autowired
    private CityService cityService;

    @Autowired
    private PropertyTaxBillable propertyTaxBillable;

    @Autowired
    private PropertyService propertyService;
    
    @Autowired
    private NoticeService noticeService;

    @Transactional
    public void initiatePropertyTransfer(final BasicProperty basicProperty, final PropertyMutation propertyMutation) {
        propertyMutation.setBasicProperty(basicProperty);
        propertyMutation.setProperty(basicProperty.getActiveProperty());
        for (final PropertyOwnerInfo ownerInfo : basicProperty.getPropertyOwnerInfo())
            propertyMutation.getTransferorInfos().add(ownerInfo.getOwner());
        propertyMutation.setMutationDate(new Date());
        if (propertyMutation.getApplicationNo() == null)
            propertyMutation.setApplicationNo(applicationNumberGenerator.generate());
        createUserIfNotExist(propertyMutation.getTransfereeInfos());
        basicProperty.getPropertyMutations().add(propertyMutation);
        basicProperty.setUnderWorkflow(true);
        processAndStoreDocument(propertyMutation.getDocuments());
        propertyService.updateIndexes(propertyMutation, APPLICATION_TYPE_TRANSFER_OF_OWNERSHIP);
        basicPropertyService.persist(basicProperty);
    }

    @Transactional
    public void approvePropertyTransfer(final BasicProperty basicProperty, final PropertyMutation propertyMutation) {
        final PropertySource propertySource = basicProperty.getPropertyOwnerInfo().get(0).getSource();
        basicProperty.getPropertyOwnerInfo().clear();
        createUserIfNotExist(propertyMutation.getTransfereeInfos());
        int order = 1;
        for (final User propertyOwner : propertyMutation.getTransfereeInfos()) {
            final PropertyOwnerInfo propertyOwnerInfo = new PropertyOwnerInfo(basicProperty, propertySource,
                    propertyOwner, order++);
            basicProperty.getPropertyOwnerInfo().add(propertyOwnerInfo);
        }
        basicPropertyService.persist(basicProperty);
    }

    @Transactional
    public void updatePropertyTransfer(final BasicProperty basicProperty, final PropertyMutation propertyMutation) {
        processAndStoreDocument(propertyMutation.getDocuments());
        checkAllMandatoryDocumentsAttached(propertyMutation);
        createUserIfNotExist(propertyMutation.getTransfereeInfos());
        basicProperty.setUnderWorkflow(true);
        propertyService.updateIndexes(propertyMutation, APPLICATION_TYPE_TRANSFER_OF_OWNERSHIP);
        basicPropertyService.persist(basicProperty);
    }

    @Transactional
    public void viewPropertyTransfer(final BasicProperty basicProperty, final PropertyMutation propertyMutation) {
        propertyService.updateIndexes(propertyMutation, APPLICATION_TYPE_TRANSFER_OF_OWNERSHIP);
        basicPropertyService.persist(basicProperty);
    }

    @Transactional
    public void deleteTransferee(final PropertyMutation propertyMutation, final Long transfereeId) {
        User userToRemove = null;
        for (final User user : propertyMutation.getTransfereeInfos())
            if (user.getId().equals(transfereeId))
                userToRemove = user;
        propertyMutation.getTransfereeInfos().remove(userToRemove);
        propertyMutationService.persist(propertyMutation);
    }

    public double calculateMutationFee(final double marketValue, final String transferReason,
            final PropertyMutation propertyMutation) {
        final int transferedInMonths = Months.monthsBetween(
                new LocalDate(propertyMutation.getMutationDate()).withDayOfMonth(1),
                new LocalDate(propertyMutation.getDeedDate()).withDayOfMonth(1)).getMonths();
        return (Double) scriptService
                .executeScript("PTIS-MUTATION-FEE-CALCULATOR", ScriptService.createContext("marketValue", marketValue,
                        "transferedInMonths", transferedInMonths, "transferReason", transferReason));
    }

    public BigDecimal getWaterTaxDues(final String wtmsTaxDueChecking_REST_url, final String upicNo) {
        final HashMap<String, Object> waterTaxInfo = simpleRestClient.getRESTResponseAsMap(wtmsTaxDueChecking_REST_url);
        return waterTaxInfo.get("totalTaxDue") == null ? BigDecimal.ZERO : new BigDecimal(
                Double.valueOf((Double) waterTaxInfo.get("totalTaxDue")));
    }

    public PropertyImpl getActiveProperty(final String upicNo) {
        return propertyImplService.findByNamedQuery("getPropertyByUpicNoAndStatus", upicNo, STATUS_ISACTIVE);
    }

    public BasicPropertyImpl getBasicPropertyByUpicNo(final String upicNo) {
        return (BasicPropertyImpl) basicPropertyDAO.getBasicPropertyByPropertyID(upicNo);
    }

    public List<DocumentType> getPropertyTransferDocumentTypes() {
        return documentTypePersistenceService.findAllByNamedQuery(DocumentType.DOCUMENTTYPE_BY_TRANSACTION_TYPE,
                TransactionType.TRANSFER);
    }

    public List<PropertyMutationMaster> getPropertyTransferReasons() {
        return propertyMutationMasterDAO.getAllPropertyMutationMastersByType(TRANSFER);
    }

    public PropertyMutationMaster getPropertyTransferReasonsByCode(String mutationCode) {
        return propertyMutationMasterDAO.getPropertyMutationMasterByCodeAndType(mutationCode, TRANSFER);
    }

    public PropertyMutation getPropertyMutationByApplicationNo(final String applicationNo) {
        return propertyMutationService.findByNamedQuery("BY_APPLICATION_NO", applicationNo);
    }

    public PropertyMutation getCurrentPropertyMutationByAssessmentNo(final String assessmentNo) {
        PropertyMutation currentPropertyMutation = null;
        BasicProperty basicProperty = getBasicPropertyByUpicNo(assessmentNo);
        if (null != basicProperty)
            for (final PropertyMutation propertyMutation : basicProperty.getPropertyMutations())
                // Checking for mutation object which is in workflow
                if (!propertyMutation.getState().getValue().equals(WF_STATE_CLOSED)) {
                    currentPropertyMutation = propertyMutation;
                    break;
                }
        return currentPropertyMutation;
    }

    public ReportOutput generateAcknowledgement(final BasicProperty basicProperty,
            final PropertyMutation propertyMutation, final String cityName, final String cityLogo) {
        final Map<String, Object> reportParams = new HashMap<String, Object>();
        final PropertyAckNoticeInfo ackBean = new PropertyAckNoticeInfo();
        ackBean.setUlbLogo(cityLogo);
        ackBean.setMunicipalityName(cityName);
        ackBean.setReceivedDate(new SimpleDateFormat("dd/MM/yyyy").format(propertyMutation.getMutationDate()));
        ackBean.setApplicationNo(propertyMutation.getApplicationNo());
        ackBean.setApplicationDate(new SimpleDateFormat("dd/MM/yyyy").format(propertyMutation.getMutationDate()));
        ackBean.setApplicationName(propertyMutation.getFullTranfereeName());
        if (propertyMutation.getTransfereeInfos() != null && propertyMutation.getTransfereeInfos().size() > 0) {
            String newOwnerName = "";
            for (final User usr : propertyMutation.getTransfereeInfos())
                newOwnerName = newOwnerName + usr.getName() + ",";
            ackBean.setOwnerName(newOwnerName.substring(0, newOwnerName.length() - 1));
        }
        ackBean.setOwnerAddress(basicProperty.getAddress().toString());
        ackBean.setNoOfDays(ptaxApplicationTypeService.findByNamedQuery(PtApplicationType.BY_CODE, TRANSFER)
                .getResolutionTime().toString());

        final ReportRequest reportInput = new ReportRequest("transferProperty_ack", ackBean, reportParams);
        reportInput.setReportFormat(FileFormat.PDF);
        return reportService.createReport(reportInput);
    }

    @Transactional
    public ReportOutput generateTransferNotice(final BasicProperty basicProperty,
            final PropertyMutation propertyMutation, final String cityName, final String cityLogo, String actionType) {
        PtNotice notice = noticeService.getNoticeByNoticeTypeAndApplicationNumber(NOTICE_TYPE_MUTATION_CERTIFICATE,
                propertyMutation.getApplicationNo());
        ReportOutput reportOutput = new ReportOutput();
        if (notice == null) {
            final PropertyAckNoticeInfo noticeBean = new PropertyAckNoticeInfo();
            noticeBean.setUlbLogo(cityLogo);
            noticeBean.setMunicipalityName(cityName);
            final Map<String, Object> reportParams = new HashMap<String, Object>();
            noticeBean.setOldOwnerName(propertyMutation.getFullTranferorName());
            noticeBean.setOldOwnerParentName(propertyMutation.getFullTransferorGuardianName());
            noticeBean.setNewOwnerName(propertyMutation.getFullTranfereeName());
            noticeBean.setNewOwnerParentName(propertyMutation.getFullTransfereeGuardianName());
            noticeBean.setRegDocDate(new SimpleDateFormat("dd/MM/yyyy").format(propertyMutation.getDeedDate()));
            noticeBean.setRegDocNo(propertyMutation.getDeedNo());
            noticeBean.setCurrentInstallment(PropertyTaxUtil.getCurrentInstallment().getDescription());
            final ReportRequest reportInput = new ReportRequest("transferProperty_notice", noticeBean, reportParams);
            reportInput.setReportFormat(FileFormat.PDF);
            reportOutput = reportService.createReport(reportInput);
            if (WFLOW_ACTION_STEP_SIGN.equalsIgnoreCase(actionType)) {
                String noticeNo = propertyTaxNumberGenerator.generateNoticeNumber(NOTICE_TYPE_MUTATION_CERTIFICATE);
                noticeService.saveNotice(propertyMutation.getApplicationNo(),noticeNo, NOTICE_TYPE_MUTATION_CERTIFICATE, basicProperty, new ByteArrayInputStream(reportOutput.getReportOutputData()));
            } 
        } else {
            final FileStoreMapper fsm = notice.getFileStore();
            final File file = fileStoreService.fetch(fsm, FILESTORE_MODULE_NAME);
            byte[] bFile;
            try {
                bFile = FileUtils.readFileToByteArray(file);
            } catch (final IOException e) {
                throw new ApplicationRuntimeException("Exception while generating Mutation Certificate : " + e);
            }
            reportOutput.setReportOutputData(bFile);
            reportOutput.setReportFormat(FileFormat.PDF);
            if (!WFLOW_ACTION_STEP_PREVIEW.equalsIgnoreCase(actionType)) {
                propertyMutation.transition().end();
                basicProperty.setUnderWorkflow(false);
            }
        }
        return reportOutput;
    }

    private void checkAllMandatoryDocumentsAttached(final PropertyMutation propertyMutation) {
        for (final Document document : propertyMutation.getDocuments())
            if ((document.getType().isMandatory() || document.isEnclosed()) && document.getFiles().isEmpty())
                throw new ValidationException(new ValidationError("documents",
                        "Please attach mandatory/marked enclosed documents."));
    }

    private void createUserIfNotExist(final List<User> transferees) {
        final List<User> newOwners = new ArrayList<>();
        transferees.forEach(transferee -> {
            if (transferee.isNew()) {
                User user = null;
                propertyMutationService.getSession().setFlushMode(FlushMode.MANUAL);
                if (null != transferee.getAadhaarNumber() && !transferee.getAadhaarNumber().isEmpty())
                    user = userService.getUserByAadhaarNumberAndType(transferee.getAadhaarNumber(),
                            transferee.getType());
                else
                    user = (User) basicPropertyService.find(
                            "From User where name = ? and mobileNumber = ? and gender = ? ", transferee.getName(),
                            transferee.getMobileNumber(), transferee.getGender());
                if (user == null) {
                    if (UserType.CITIZEN.equals(transferee.getType())) {
                        final Citizen newOwner = new Citizen();
                        newOwner.setAadhaarNumber(transferee.getAadhaarNumber());
                        newOwner.setEmailId(transferee.getEmailId());
                        newOwner.setMobileNumber(transferee.getMobileNumber());
                        newOwner.setGender(transferee.getGender());
                        newOwner.setGuardian(transferee.getGuardian());
                        newOwner.setGuardianRelation(transferee.getGuardianRelation());
                        newOwner.setSalutation(transferee.getSalutation());
                        newOwner.setName(transferee.getName());
                        newOwner.setPassword("NOTSET");
                        newOwner.setUsername(propertyTaxUtil.generateUserName(transferee.getName()));
                        newOwners.add(newOwner);
                    }
                } else {
                    user.setEmailId(transferee.getEmailId());
                    user.setGuardian(transferee.getGuardian());
                    user.setGuardianRelation(transferee.getGuardianRelation());
                    newOwners.add(user);
                }
            } else
                newOwners.add(transferee);
        });
        propertyMutationService.getSession().setFlushMode(FlushMode.AUTO);
        transferees.clear();
        transferees.addAll(newOwners);
    }

    private void processAndStoreDocument(final List<Document> documents) {
        documents.forEach(document -> {
            if (!document.getUploads().isEmpty()) {
                int fileCount = 0;
                for (final File file : document.getUploads()) {
                    final FileStoreMapper fileStore = fileStoreService.store(file,
                            document.getUploadsFileName().get(fileCount),
                            document.getUploadsContentType().get(fileCount++), FILESTORE_MODULE_NAME);
                    document.getFiles().add(fileStore);
                }
            }
            if (document.getId() == null || document.getType() == null) {
                document.setType(documentTypePersistenceService.load(document.getType().getId(), DocumentType.class));
            }
        });
    }

    public String generateReceipt(final PropertyMutation propertyMutation) {
        propertyTaxBillable.setBasicProperty(propertyMutation.getBasicProperty());
        propertyTaxBillable.setMutationFeePayment(Boolean.TRUE);
        propertyTaxBillable.setMutationFee(propertyMutation.getMutationFee());
        propertyTaxBillable.setCollectionType(DemandConstants.COLLECTIONTYPE_COUNTER);
        propertyTaxBillable.setCallbackForApportion(Boolean.FALSE);
        propertyTaxBillable.setMutationApplicationNo(propertyMutation.getApplicationNo());
        propertyTaxBillable.setUserId(EgovThreadLocals.getUserId());
        propertyTaxBillable.setReferenceNumber(propertyTaxNumberGenerator.generateManualBillNumber(propertyMutation
                .getBasicProperty().getPropertyID()));
        return ptBillServiceImpl.getBillXML(propertyTaxBillable);
    }

    public String getLoggedInUserDesignation() {
        final Designation designation = propertyTaxUtil.getDesignationForUser(securityUtils.getCurrentUser().getId());
        return designation.getName();
    }

    public User getLoggedInUser() {
        return securityUtils.getCurrentUser();
    }

    @Transactional
    public void updateMutationCollection(final PropertyMutation propertyMutation) {
        propertyMutationService.persist(propertyMutation);
    }

    public String getCityName() {
        return cityService.getCityByURL(EgovThreadLocals.getDomainName()).getName();
    }

    public Designation getUserDesigantion() {
        final Long userId = securityUtils.getCurrentUser().getId();
        final Designation designation = propertyTaxUtil.getDesignationForUser(userId);
        return designation;
    }

    public PropertyMutation initiatePropertyTransfer(BasicProperty basicproperty, PropertyMutation propertyMutation,
            HashMap<String, String> meesevaParams) {
        initiatePropertyTransfer(basicproperty, propertyMutation);
        return propertyMutation;
    }

}
