/*******************************************************************************
 * eGov suite of products aim to improve the internal efficiency,transparency,
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
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 ******************************************************************************/
/**
 *
 */
package org.egov.ptis.actions.objection;

import static org.egov.ptis.constants.PropertyTaxConstants.CURR_DMD_STR;
import static org.egov.ptis.constants.PropertyTaxConstants.DEVIATION_PERCENTAGE;
import static org.egov.ptis.constants.PropertyTaxConstants.FILESTORE_MODULE_NAME;
import static org.egov.ptis.constants.PropertyTaxConstants.FLOOR_MAP;
import static org.egov.ptis.constants.PropertyTaxConstants.HEARING_TIMINGS;
import static org.egov.ptis.constants.PropertyTaxConstants.NON_VAC_LAND_PROPERTY_TYPE_CATEGORY;
import static org.egov.ptis.constants.PropertyTaxConstants.OWNERSHIP_TYPE_VAC_LAND;
import static org.egov.ptis.constants.PropertyTaxConstants.PROPERTY_MODIFY_REASON_OBJ;
import static org.egov.ptis.constants.PropertyTaxConstants.STATUS_ISACTIVE;
import static org.egov.ptis.constants.PropertyTaxConstants.STATUS_ISHISTORY;
import static org.egov.ptis.constants.PropertyTaxConstants.STATUS_WORKFLOW;
import static org.egov.ptis.constants.PropertyTaxConstants.VAC_LAND_PROPERTY_TYPE_CATEGORY;
import static org.egov.ptis.constants.PropertyTaxConstants.WFLOW_ACTION_STEP_APPROVE;
import static org.egov.ptis.constants.PropertyTaxConstants.WFLOW_ACTION_STEP_REJECT;
import static org.egov.ptis.constants.PropertyTaxConstants.WFLOW_ACTION_STEP_SAVE;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.egov.commons.Area;
import org.egov.commons.EgwStatus;
import org.egov.commons.dao.EgwStatusHibernateDAO;
import org.egov.demand.model.EgDemandDetails;
import org.egov.eis.entity.Assignment;
import org.egov.eis.service.DesignationService;
import org.egov.eis.service.EisCommonService;
import org.egov.eis.service.EmployeeService;
import org.egov.eis.service.PositionMasterService;
import org.egov.infra.admin.master.entity.User;
import org.egov.infra.admin.master.service.UserService;
import org.egov.infra.exception.ApplicationRuntimeException;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.messaging.MessagingService;
import org.egov.infra.persistence.entity.Address;
import org.egov.infra.reporting.engine.ReportConstants;
import org.egov.infra.reporting.engine.ReportConstants.FileFormat;
import org.egov.infra.reporting.engine.ReportOutput;
import org.egov.infra.reporting.engine.ReportRequest;
import org.egov.infra.reporting.engine.ReportService;
import org.egov.infra.reporting.viewer.ReportViewerUtil;
import org.egov.infra.security.utils.SecurityUtils;
import org.egov.infra.utils.ApplicationNumberGenerator;
import org.egov.infra.utils.EgovThreadLocals;
import org.egov.infra.web.struts.annotation.ValidationErrorPage;
import org.egov.infra.web.utils.WebUtils;
import org.egov.infra.workflow.entity.StateHistory;
import org.egov.infra.workflow.service.SimpleWorkflowService;
import org.egov.infra.workflow.service.WorkflowService;
import org.egov.infstr.services.PersistenceService;
import org.egov.infstr.utils.DateUtils;
import org.egov.infstr.workflow.WorkFlowMatrix;
import org.egov.pims.commons.Position;
import org.egov.ptis.actions.common.PropertyTaxBaseAction;
import org.egov.ptis.actions.view.ViewPropertyAction;
import org.egov.ptis.bean.PropertyNoticeInfo;
import org.egov.ptis.client.util.PropertyTaxNumberGenerator;
import org.egov.ptis.client.util.PropertyTaxUtil;
import org.egov.ptis.constants.PropertyTaxConstants;
import org.egov.ptis.domain.dao.demand.PtDemandDao;
import org.egov.ptis.domain.dao.property.BasicPropertyDAO;
import org.egov.ptis.domain.dao.property.PropertyStatusDAO;
import org.egov.ptis.domain.dao.property.PropertyStatusValuesDAO;
import org.egov.ptis.domain.entity.demand.Ptdemand;
import org.egov.ptis.domain.entity.enums.TransactionType;
import org.egov.ptis.domain.entity.objection.RevisionPetition;
import org.egov.ptis.domain.entity.property.Apartment;
import org.egov.ptis.domain.entity.property.BasicProperty;
import org.egov.ptis.domain.entity.property.BasicPropertyImpl;
import org.egov.ptis.domain.entity.property.BuiltUpProperty;
import org.egov.ptis.domain.entity.property.DocumentType;
import org.egov.ptis.domain.entity.property.Floor;
import org.egov.ptis.domain.entity.property.FloorType;
import org.egov.ptis.domain.entity.property.Property;
import org.egov.ptis.domain.entity.property.PropertyDetail;
import org.egov.ptis.domain.entity.property.PropertyID;
import org.egov.ptis.domain.entity.property.PropertyImpl;
import org.egov.ptis.domain.entity.property.PropertyMutationMaster;
import org.egov.ptis.domain.entity.property.PropertyOccupation;
import org.egov.ptis.domain.entity.property.PropertyStatusValues;
import org.egov.ptis.domain.entity.property.PropertyTypeMaster;
import org.egov.ptis.domain.entity.property.PropertyUsage;
import org.egov.ptis.domain.entity.property.RoofType;
import org.egov.ptis.domain.entity.property.StructureClassification;
import org.egov.ptis.domain.entity.property.VacantProperty;
import org.egov.ptis.domain.entity.property.WallType;
import org.egov.ptis.domain.entity.property.WoodType;
import org.egov.ptis.domain.service.notice.NoticeService;
import org.egov.ptis.domain.service.property.PropertyService;
import org.egov.ptis.domain.service.property.SMSEmailService;
import org.egov.ptis.domain.service.revisionPetition.RevisionPetitionService;
import org.egov.ptis.notice.PtNotice;
import org.egov.ptis.report.bean.PropertyAckNoticeInfo;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author pradeep
 */
@ParentPackage("egov")
@Results({ @Result(name = "new", location = "revisionPetition-new.jsp"),
        @Result(name = "message", location = "revisionPetition-message.jsp"),
        @Result(name = "notice", location = "revisionPetition-notice.jsp"),
        @Result(name = "view", location = "revisionPetition-view.jsp"),
        @Result(name = RevisionPetitionAction.COMMON_FORM, location = "search/searchProperty-commonForm.jsp") })
public class RevisionPetitionAction extends PropertyTaxBaseAction {

    private static final long serialVersionUID = 1L;
    protected static final String COMMON_FORM = "commonForm";
    private final String REJECTED = "Rejected";
    public static final String STRUTS_RESULT_MESSAGE = "message";
    private static final String REVISION_PETITION_CREATED = "CREATED";
    private static final String REVISION_PETITION_HEARINGNOTICEGENERATED = "HEARINGNOTICEGENERATED";
    private static final String REVISION_PETITION_ENDORESEMENTGENERATED = "ENDORESEMTNTGENERATED";
    public static final String NOTICE = "notice";
    private final Logger LOGGER = Logger.getLogger(RevisionPetitionAction.class);
    private ViewPropertyAction viewPropertyAction = new ViewPropertyAction();
    private RevisionPetition objection = new RevisionPetition();
    private String propertyId;
    private Map<String, Object> viewMap;
    private RevisionPetitionService revisionPetitionService;
    protected WorkflowService<RevisionPetition> objectionWorkflowService;
    private String ownerName;
    private String propertyAddress;
    private PersistenceService<Property, Long> propertyImplService;
    private String propTypeObjId;
    final SimpleDateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy");
    private String[] floorNoStr = new String[100];
    private Boolean loggedUserIsEmployee = Boolean.TRUE;
    private PropertyService propService;
    private PropertyStatusValues propStatVal;
    private String reasonForModify;
    private TreeMap<Integer, String> floorNoMap;
    private Map<String, String> deviationPercentageMap;
    private TreeMap<String, String> hearingTimingMap;
    private String areaOfPlot;

    private List<DocumentType> documentTypes = new ArrayList<>();
    private String northBoundary;
    private String southBoundary;
    private String eastBoundary;
    private String westBoundary;
    private Map<String, String> propTypeCategoryMap;
    private Integer reportId = -1;
    private Long taxExemptedReason;

    @Autowired
    private PropertyStatusValuesDAO propertyStatusValuesDAO;
    @Autowired
    private ReportService reportService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private BasicPropertyDAO basicPropertyDAO;
    private PropertyTaxNumberGenerator propertyTaxNumberGenerator;
    @Autowired
    protected SimpleWorkflowService<RevisionPetition> revisionPetitionWorkFlowService;

    private boolean isShowAckMessage;
    @Autowired
    private PtDemandDao ptDemandDAO;
    @Autowired
    private SecurityUtils securityUtils;
    @Autowired
    private UserService userService;

    @Autowired
    private PropertyStatusDAO propertyStatusDAO;

    @Autowired
    private EgwStatusHibernateDAO egwStatusDAO;
    @Autowired
    private EisCommonService eisCommonService;
    @Autowired
    PositionMasterService positionMasterService;

    @Autowired
    DesignationService designationService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private ApplicationNumberGenerator applicationNumberGenerator;
    @Autowired
    private MessagingService messagingService;
    private SMSEmailService sMSEmailService;

    public RevisionPetitionAction() {

        addRelatedEntity("basicProperty", BasicPropertyImpl.class);
        addRelatedEntity("property.propertyDetail.propertyTypeMaster", PropertyTypeMaster.class);
        addRelatedEntity("property.propertyDetail.sitalArea", Area.class);

        addRelatedEntity("property", PropertyImpl.class);
        addRelatedEntity("property.propertyDetail.floorType", FloorType.class);
        addRelatedEntity("property.propertyDetail.roofType", RoofType.class);
        addRelatedEntity("property.propertyDetail.wallType", WallType.class);
        addRelatedEntity("property.propertyDetail.woodType", WoodType.class);
        this.addRelatedEntity("structureClassification", StructureClassification.class);
        this.addRelatedEntity("property.propertyDetail.apartment", Apartment.class);
    }

    @Override
    public RevisionPetition getModel() {

        return objection;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void prepare() {
        User user = null;
        // to merge the new values from jsp with existing
        if (objection.getId() != null)
            objection = revisionPetitionService.findById(objection.getId(), false);

        if (objection != null && objection.getId() == null) {
            objection.setRecievedOn(new Date());
            user = securityUtils.getCurrentUser();
            if (user != null)
                objection.setRecievedBy(user.getUsername());
        }

        loggedUserIsEmployee = propService.isEmployee(securityUtils.getCurrentUser());
        super.prepare();
        setUserInfo();
        // setupWorkflowDetails();
        documentTypes = propService.getDocumentTypesForTransactionType(TransactionType.MODIFY);
        final List<WallType> wallTypes = getPersistenceService().findAllBy("from WallType order by name");
        final List<WoodType> woodTypes = getPersistenceService().findAllBy("from WoodType order by name");
        final List<PropertyTypeMaster> propTypeList = getPersistenceService().findAllBy(
                "from PropertyTypeMaster order by orderNo");
        final List<PropertyMutationMaster> propMutList = getPersistenceService().findAllBy(
                "from PropertyMutationMaster where type = 'MODIFY' and code in('OBJ')");
        final List<String> StructureList = getPersistenceService().findAllBy("from StructureClassification");
        final List<PropertyUsage> usageList = getPersistenceService()
                .findAllBy("from PropertyUsage order by usageName");
        final List<PropertyOccupation> propOccList = getPersistenceService().findAllBy("from PropertyOccupation");
        final List<String> ageFacList = getPersistenceService().findAllBy("from DepreciationMaster");
        setFloorNoMap(FLOOR_MAP);
        addDropdownData("floorType", getPersistenceService().findAllBy("from FloorType order by name"));
        addDropdownData("roofType", getPersistenceService().findAllBy("from RoofType order by name"));
        final List<String> apartmentsList = getPersistenceService().findAllBy("from Apartment order by name");
        final List<String> taxExemptionReasonList = getPersistenceService().findAllBy(
                "from TaxExeptionReason order by name");

        addDropdownData("wallType", wallTypes);
        addDropdownData("woodType", woodTypes);
        addDropdownData("PropTypeMaster", propTypeList);
        addDropdownData("OccupancyList", propOccList);
        addDropdownData("UsageList", usageList);
        addDropdownData("MutationList", propMutList);
        addDropdownData("StructureList", StructureList);
        addDropdownData("AgeFactorList", ageFacList);
        addDropdownData("apartments", apartmentsList);
        addDropdownData("taxExemptionReasonList", taxExemptionReasonList);

        populatePropertyTypeCategory();
        setDeviationPercentageMap(DEVIATION_PERCENTAGE);
        setHearingTimingMap(HEARING_TIMINGS);

    }

    /**
     * Method to create new revision petition.
     *
     * @return NEW
     */
    @SkipValidation
    @Action(value = "/revPetition/revPetition-newForm")
    public String newForm() {

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Entered into newForm");
        final BasicProperty basicProperty = basicPropertyDAO.getBasicPropertyByIndexNumAndParcelID(propertyId, null);
        if (basicProperty.getProperty().getStatus().equals(PropertyTaxConstants.STATUS_ISACTIVE)) {
            addActionError(getText("revPetition.demandActive"));
            return COMMON_FORM;
        }

        getPropertyView(propertyId);

        if (objection != null && objection.getBasicProperty() != null && objection.getBasicProperty().isUnderWorkflow()) {
            addActionMessage(getText("property.state.objected",
                    new String[] { objection.getBasicProperty().getUpicNo() }));
            return STRUTS_RESULT_MESSAGE;
        }
        setFloorDetails(objection.getBasicProperty().getProperty());
        return NEW;
    }

    /**
     * Create revision petition.
     *
     * @return
     */
    @Action(value = "/revPetition/revPetition")
    public String create() {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("ObjectionAction | Create | start " + objection);
        // setupWorkflowDetails();

        if (objection != null && objection.getBasicProperty() != null && objection.getState() == null
                && objection.getBasicProperty().isUnderWorkflow()) {
            addActionMessage(getText("property.state.objected",
                    new String[] { objection.getBasicProperty().getUpicNo() }));
            return STRUTS_RESULT_MESSAGE;
        }

        if (objection.getRecievedOn() == null) {
            addActionMessage(getText("mandatory.fieldvalue.receivedOn"));
            return NEW;
        }
        objection.setObjectionNumber(applicationNumberGenerator.generate());
        objection.getBasicProperty().setStatus(
                propertyStatusDAO.getPropertyStatusByCode(PropertyTaxConstants.STATUS_OBJECTED_STR));
        objection.getBasicProperty().setUnderWorkflow(Boolean.TRUE);
        updateStateAndStatus(objection);
        addActionMessage(getText("objection.success") + objection.getObjectionNumber());
        revisionPetitionService.applyAuditing(objection.getState());
        revisionPetitionService.createRevisionPetition(objection);
        sendEmailandSms(objection, REVISION_PETITION_CREATED);
        // objectionService.persist(objection);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("ObjectionAction | Create | End " + objection);
        // }
        return STRUTS_RESULT_MESSAGE;
    }

    /**
     * Method to add hearing date
     *
     * @return
     */
    @Action(value = "/revPetition/revPetition-addHearingDate")
    public String addHearingDate() {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("ObjectionAction | addHearingDate | start " + objection);
        InputStream hearingNoticePdf = null;
        ReportOutput reportOutput = new ReportOutput();

        // Commented for time being. Client confirmation required.
        /*
         * if (objection.getHearings().get(objection.getHearings().size() -
         * 1).getPlannedHearingDt() .before(objection.getRecievedOn())) {
         * objection.setHearings(Collections.emptyList());
         * addActionMessage(getText
         * ("receivedon.greaterThan.plannedhearingdate").
         * concat("").concat(dateformat.format(objection.getRecievedOn())));
         * return "view"; } else {
         */
        updateStateAndStatus(objection);
        reportOutput = createHearingNoticeReport(reportOutput, objection);
        if (reportOutput != null && reportOutput.getReportOutputData() != null)
            hearingNoticePdf = new ByteArrayInputStream(reportOutput.getReportOutputData());
        if (hearingNoticePdf != null)
            noticeService.saveNotice(objection.getObjectionNumber(), objection.getObjectionNumber(),
                    PropertyTaxConstants.NOTICE_TYPE_REVISIONPETITION_HEARINGNOTICE, objection.getBasicProperty(),
                    hearingNoticePdf);// Save Notice
        revisionPetitionService.updateRevisionPetition(objection);
        sendEmailandSms(objection, REVISION_PETITION_HEARINGNOTICEGENERATED);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("ObjectionAction | addHearingDate | End " + objection);
        // }
        return STRUTS_RESULT_MESSAGE;
    }

    /**
     * @param reportOutput
     * @param objection
     * @return ReportOutput
     */
    private ReportOutput createHearingNoticeReport(ReportOutput reportOutput, final RevisionPetition objection) {
        reportOutput.setReportFormat(FileFormat.PDF);
        final HashMap<String, Object> reportParams = new HashMap<String, Object>();

        ReportRequest reportRequest = null;

        if (objection != null) {

            final HttpServletRequest request = ServletActionContext.getRequest();
            final String url = WebUtils.extractRequestDomainURL(request, false);
            final String cityLogo = url.concat(PropertyTaxConstants.IMAGE_CONTEXT_PATH).concat(
                    (String) request.getSession().getAttribute("citylogo"));
            final String cityName = request.getSession().getAttribute("citymunicipalityname").toString();
            reportParams.put("logoPath", cityLogo);
            reportParams.put("cityName", cityName);
            reportParams.put("recievedBy", objection.getBasicProperty().getFullOwnerName());

            if (objection.getHearings() != null && objection.getHearings().size() > 0
                    && objection.getHearings().get(objection.getHearings().size() - 1).getPlannedHearingDt() != null)
                reportParams.put(
                        "hearingNoticeDate",
                        dateformat.format(objection.getHearings().get(objection.getHearings().size() - 1)
                                .getPlannedHearingDt()));
            else
                reportParams.put("hearingNoticeDate", "");
            reportParams.put("currentDate", dateformat.format(new Date()));
            reportParams.put("receivedOn", dateformat.format(objection.getRecievedOn()));
            reportParams.put("docNumberObjection", objection.getObjectionNumber());
            reportParams.put("HouseNo", objection.getBasicProperty().getUpicNo());
            reportParams.put("hearingTime", objection.getHearings().get(objection.getHearings().size() - 1)
                    .getHearingTime());
            reportParams.put("hearingVenue", objection.getHearings().get(objection.getHearings().size() - 1)
                    .getHearingVenue());

            reportRequest = new ReportRequest(PropertyTaxConstants.REPORT_TEMPLATENAME_REVISIONPETITION_HEARINGNOTICE,
                    objection, reportParams);
            reportOutput = reportService.createReport(reportRequest);
        }
        return reportOutput;
    }

    /**
     * Generate Hearing notice
     *
     * @return
     */
    @ValidationErrorPage(value = "view")
    @Action(value = "/revPetition/revPetition-generateHearingNotice")
    public String generateHearingNotice() {
        objection.setGenerateSpecialNotice(Boolean.TRUE);
        updateStateAndStatus(objection);
        final PropertyImpl refNewProperty = propService.creteNewPropertyForObjectionWorkflow(
                objection.getBasicProperty(), objection.getObjectionNumber(), objection.getRecievedOn(),
                objection.getCreatedBy(), null, PROPERTY_MODIFY_REASON_OBJ);
        propertyImplService.getSession().flush();
        objection.setProperty(refNewProperty);
        revisionPetitionService.updateRevisionPetition(objection);
        return STRUTS_RESULT_MESSAGE;

    }

    @ValidationErrorPage(value = "view")
    @Action(value = "/revPetition/revPetition-printHearingNotice")
    public String printHearingNotice() {

        objection = revisionPetitionService.findById(Long.valueOf(parameters.get("objectionId")[0]), false);

        final ReportOutput reportOutput = new ReportOutput();
        if (objection != null && objection.getObjectionNumber() != null) {
            final PtNotice ptNotice = noticeService.getPtNoticeByNoticeNumberAndNoticeType(
                    objection.getObjectionNumber(), PropertyTaxConstants.NOTICE_TYPE_REVISIONPETITION_HEARINGNOTICE);

            final FileStoreMapper fsm = ptNotice.getFileStore();
            final File file = fileStoreService.fetch(fsm, FILESTORE_MODULE_NAME);
            byte[] bFile;
            try {
                bFile = FileUtils.readFileToByteArray(file);
            } catch (final IOException e) {
                e.printStackTrace();
                throw new ApplicationRuntimeException("Exception while generating Hearing Notcie : " + e);
            }
            reportOutput.setReportOutputData(bFile);
            reportOutput.setReportFormat(FileFormat.PDF);
            getSession().remove(ReportConstants.ATTRIB_EGOV_REPORT_OUTPUT_MAP);
            reportId = ReportViewerUtil.addReportToSession(reportOutput, getSession());
        } else
            addActionMessage(getText("objection.nohearingNotice"));
        return NOTICE;
    }

    @ValidationErrorPage(value = "view")
    @Action(value = "/revPetition/revPetition-recordHearingDetails")
    public String recordHearingDetails() {

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("ObjectionAction | recordHearingDetails | start "
                    + objection.getHearings().get(objection.getHearings().size() - 1));
        vaidatePropertyDetails();

        if (hasErrors())
            return "view";

        // set the auto generated hearing number
        if (null == objection.getHearings().get(objection.getHearings().size() - 1).getHearingNumber()) {
            final String hearingNumber = applicationNumberGenerator.generate();
            objection.getHearings().get(objection.getHearings().size() - 1).setHearingNumber(hearingNumber);
            addActionMessage(getText("hearingNum") + " " + hearingNumber);
        }

        updateStateAndStatus(objection);
        modifyBasicProp();
        propertyImplService.merge(objection.getProperty());
        revisionPetitionService.updateRevisionPetition(objection);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("ObjectionAction | recordHearingDetails | End "
                    + objection.getHearings().get(objection.getHearings().size() - 1));
        return STRUTS_RESULT_MESSAGE;

    }

    /**
     * @description - allows the user to record the inspection details.
     * @return String
     */
    @ValidationErrorPage(value = "view")
    @Action(value = "/revPetition/revPetition-recordInspectionDetails")
    public String recordInspectionDetails() {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("ObjectionAction | recordInspectionDetails | start "
                    + objection.getInspections().get(objection.getInspections().size() - 1));
        vaidatePropertyDetails();

        if (hasErrors())
            return "view";
        updateStateAndStatus(objection);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("ObjectionAction | recordInspectionDetails | End "
                    + objection.getInspections().get(objection.getInspections().size() - 1));
        modifyBasicProp();
        propertyImplService.merge(objection.getProperty());
        revisionPetitionService.updateRevisionPetition(objection);
        return STRUTS_RESULT_MESSAGE;
    }

    @ValidationErrorPage(value = "view")
    @Action(value = "/revPetition/revPetition-validateInspectionDetails")
    public String validateInspectionDetails() {
        updateStateAndStatus(objection);
        revisionPetitionService.updateRevisionPetition(objection);
        return STRUTS_RESULT_MESSAGE;
    }

    @ValidationErrorPage(value = "view")
    @Action(value = "/revPetition/revPetition-rejectInspectionDetails")
    public String rejectInspectionDetails() {

        updateStateAndStatus(objection);
        revisionPetitionService.updateRevisionPetition(objection);
        return STRUTS_RESULT_MESSAGE;
    }

    /**
     * Allows the user to record whether the objection is accepted or rejected.
     *
     * @return
     */
    @ValidationErrorPage(value = "view")
    @Action(value = "/revPetition/revPetition-recordObjectionOutcome")
    public String recordObjectionOutcome() {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("ObjectionAction | recordObjectionOutcome | start " + objection);

        if (hasErrors())
            return "view";

        if (objection.getDateOfOutcome() != null && objection.getDateOfOutcome().after(new Date())) {
            addActionMessage(getText("dateOfOutcome.greaterThan.todaydate"));
            return "view";
        }
        // Commented for time being. Client confirmation required.
        /*
         * else if (objection.getDateOfOutcome() != null &&
         * objection.getRecievedOn().after(objection.getDateOfOutcome())) {
         * addActionMessage
         * (getText("dateOfOutcome.greaterThan.recievedOn").concat
         * (" ").concat(dateformat.format(objection.getRecievedOn()))); return
         * "view"; }
         */

        if (WFLOW_ACTION_STEP_APPROVE.equalsIgnoreCase(workFlowAction))
            objection.setEgwStatus(egwStatusDAO.getStatusByModuleAndCode(PropertyTaxConstants.OBJECTION_MODULE,
                    PropertyTaxConstants.OBJECTION_ACCEPTED));
        updateStateAndStatus(objection);
        revisionPetitionService.updateRevisionPetition(objection);
        sendEmailandSms(objection, REVISION_PETITION_ENDORESEMENTGENERATED);
        addActionMessage(getText("objection.outcome.success"));
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("ObjectionAction | recordObjectionOutcome | End " + objection);
        return STRUTS_RESULT_MESSAGE;
    }

    /**
     * @param reportOutput
     * @param objection
     * @return
     */
    private ReportOutput createEndoresement(ReportOutput reportOutput, final RevisionPetition objection) {

        reportOutput.setReportFormat(FileFormat.PDF);
        final HashMap<String, Object> reportParams = new HashMap<String, Object>();

        ReportRequest reportRequest = null;
        if (objection != null) {
            final Map<String, BigDecimal> currentDemand = ptDemandDAO.getDemandCollMap(objection.getProperty());
            final Map<String, BigDecimal> earlierDemand = ptDemandDAO.getDemandCollMap(objection.getBasicProperty()
                    .getProperty());
            final HttpServletRequest request = ServletActionContext.getRequest();
            final String url = WebUtils.extractRequestDomainURL(request, false);
            final String cityLogo = url.concat(PropertyTaxConstants.IMAGE_CONTEXT_PATH).concat(
                    (String) request.getSession().getAttribute("citylogo"));
            final String cityName = request.getSession().getAttribute("citymunicipalityname").toString();

            reportParams.put("logoPath", cityLogo);
            reportParams.put("cityName", cityName);

            reportParams.put("recievedBy", objection.getRecievedBy());
            reportParams.put("docNumberObjection", objection.getObjectionNumber());

            reportParams.put("currentDate", dateformat.format(new Date()));
            reportParams.put("receivedOn", dateformat.format(objection.getRecievedOn()));
            reportParams.put("HouseNo", objection.getBasicProperty().getUpicNo());
            reportParams.put("wardNumber", objection.getBasicProperty().getBoundary() != null ? objection
                    .getBasicProperty().getBoundary().getName() : "");
            reportParams.put("HalfYearPropertyTaxTo", currentDemand.get(CURR_DMD_STR).divide(BigDecimal.valueOf(2))
                    .setScale(2));
            reportParams.put("HalfYearPropertyTaxFrom", earlierDemand.get(CURR_DMD_STR).divide(BigDecimal.valueOf(2))
                    .setScale(2));
            reportParams.put("AnnualPropertyTaxTo", currentDemand.get(CURR_DMD_STR).setScale(2).toString());
            reportParams.put("AnnualPropertyTaxFrom", earlierDemand.get(CURR_DMD_STR).setScale(2).toString());

            reportRequest = new ReportRequest(PropertyTaxConstants.REPORT_TEMPLATENAME_REVISIONPETITION_ENDORSEMENT,
                    objection, reportParams);
            reportOutput = reportService.createReport(reportRequest);
        }
        return reportOutput;

    }

    /**
     * @param property
     * @param basicProperty
     */
    public void generateSpecialNotice(final PropertyImpl property, final BasicPropertyImpl basicProperty) {
        final Map<String, Object> reportParams = new HashMap<String, Object>();
        ReportRequest reportInput = null;
        PropertyNoticeInfo propertyNotice = null;
        InputStream specialNoticePdf = null;

        final String noticeNo = propertyTaxNumberGenerator
                .generateNoticeNumber(PropertyTaxConstants.NOTICE_TYPE_SPECIAL_NOTICE);
        propertyNotice = new PropertyNoticeInfo(property, noticeNo);

        final HttpServletRequest request = ServletActionContext.getRequest();
        final String url = WebUtils.extractRequestDomainURL(request, false);
        final String imagePath = url.concat(PropertyTaxConstants.IMAGE_CONTEXT_PATH).concat(
                (String) request.getSession().getAttribute("citylogo"));
        final String cityName = request.getSession().getAttribute("citymunicipalityname").toString();
        reportParams.put("cityName", cityName);
        reportParams.put("logoPath", imagePath);
        reportParams.put("mode", "create");

        setNoticeInfo(property, propertyNotice, basicProperty);
        final List<PropertyAckNoticeInfo> floorDetails = getFloorDetailsForNotice(property, propertyNotice
                .getOwnerInfo().getTotalTax());
        propertyNotice.setFloorDetailsForNotice(floorDetails);
        reportInput = new ReportRequest(PropertyTaxConstants.REPORT_TEMPLATENAME_SPECIAL_NOTICE, propertyNotice,
                reportParams);
        reportInput.setPrintDialogOnOpenReport(true);
        reportInput.setReportFormat(FileFormat.PDF);
        final ReportOutput reportOutput = reportService.createReport(reportInput);

        if (reportOutput != null && reportOutput.getReportOutputData() != null)
            specialNoticePdf = new ByteArrayInputStream(reportOutput.getReportOutputData());
        noticeService.saveNotice(
                objection.getObjectionNumber(),
                objection.getObjectionNumber().concat(
                        PropertyTaxConstants.NOTICE_TYPE_REVISIONPETITION_SPECIALNOTICE_PREFIX),
                PropertyTaxConstants.NOTICE_TYPE_SPECIAL_NOTICE, objection.getBasicProperty(), specialNoticePdf);
    }

    /**
     * @param property
     * @param totalTax
     * @return
     */
    private List<PropertyAckNoticeInfo> getFloorDetailsForNotice(final PropertyImpl property, final BigDecimal totalTax) {
        final List<PropertyAckNoticeInfo> floorDetailsList = new ArrayList<PropertyAckNoticeInfo>();
        final PropertyDetail detail = property.getPropertyDetail();
        PropertyAckNoticeInfo floorInfo = null;
        for (final Floor floor : detail.getFloorDetails()) {
            floorInfo = new PropertyAckNoticeInfo();
            floorInfo.setBuildingClassification(floor.getStructureClassification().getTypeName());
            floorInfo.setNatureOfUsage(floor.getPropertyUsage().getUsageName());
            floorInfo.setPlinthArea(new BigDecimal(floor.getBuiltUpArea().getArea()));
            floorInfo.setBuildingAge(floor.getDepreciationMaster().getDepreciationName());
            floorInfo.setMonthlyRentalValue(BigDecimal.ZERO);
            floorInfo.setYearlyRentalValue(BigDecimal.ZERO);
            floorInfo.setTaxPayableForCurrYear(totalTax);
            floorInfo.setTaxPayableForNewRates(BigDecimal.ZERO);

            floorDetailsList.add(floorInfo);
        }
        return floorDetailsList;
    }

    /**
     * @param property
     * @param propertyNotice
     * @param basicProperty
     */
    private void setNoticeInfo(final PropertyImpl property, final PropertyNoticeInfo propertyNotice,
            final BasicPropertyImpl basicProperty) {
        final PropertyAckNoticeInfo ownerInfo = new PropertyAckNoticeInfo();
        final Address ownerAddress = basicProperty.getAddress();

        if (basicProperty.getPropertyOwnerInfo().size() > 1)
            ownerInfo.setOwnerName(basicProperty.getFullOwnerName().concat(" and others"));
        else
            ownerInfo.setOwnerName(basicProperty.getFullOwnerName());

        ownerInfo.setOwnerAddress(basicProperty.getAddress().toString());
        ownerInfo.setApplicationNo(property.getApplicationNo());
        ownerInfo.setDoorNo(ownerAddress.getHouseNoBldgApt());
        if (org.apache.commons.lang.StringUtils.isNotBlank(ownerAddress.getLandmark()))
            ownerInfo.setStreetName(ownerAddress.getLandmark());
        else
            ownerInfo.setStreetName("N/A");
        final SimpleDateFormat formatNowYear = new SimpleDateFormat("yyyy");
        final String occupancyYear = formatNowYear.format(basicProperty.getPropOccupationDate());
        ownerInfo.setInstallmentYear(occupancyYear);
        ownerInfo.setAssessmentNo(basicProperty.getUpicNo());
        ownerInfo.setAssessmentDate(dateformat.format(basicProperty.getAssessmentdate()).toString());
        final Ptdemand currDemand = ptDemandDAO.getNonHistoryCurrDmdForProperty(property);
        BigDecimal totalTax = BigDecimal.ZERO;
        for (final EgDemandDetails demandDetail : currDemand.getEgDemandDetails()) {
            totalTax = totalTax.add(demandDetail.getAmount());
            if (demandDetail.getEgDemandReason().getEgDemandReasonMaster().getReasonMaster().toUpperCase()
                    .equalsIgnoreCase(PropertyTaxConstants.DEMANDRSN_STR_EDUCATIONAL_CESS))
                ownerInfo.setEducationTax(demandDetail.getAmount());
            if (demandDetail.getEgDemandReason().getEgDemandReasonMaster().getReasonMaster().toUpperCase()
                    .equalsIgnoreCase(PropertyTaxConstants.DEMANDRSN_STR_LIBRARY_CESS))
                ownerInfo.setLibraryTax(demandDetail.getAmount());
            if (demandDetail.getEgDemandReason().getEgDemandReasonMaster().getReasonMaster().toUpperCase()
                    .equalsIgnoreCase(PropertyTaxConstants.DEMANDRSN_STR_GENERAL_TAX))
                ownerInfo.setGeneralTax(demandDetail.getAmount());
        }
        ownerInfo.setTotalTax(totalTax);
        final PropertyID propertyId = basicProperty.getPropertyID();
        ownerInfo.setZoneName(propertyId.getZone().getName());
        ownerInfo.setWardName(propertyId.getWard().getName());
        ownerInfo.setAreaName(propertyId.getArea().getName());
        ownerInfo.setLocalityName(propertyId.getLocality().getName());
        ownerInfo.setNoticeDate(new Date());

        propertyNotice.setOwnerInfo(ownerInfo);
    }

    @Action(value = "/revPetition/revPetition-printEnodresementNotice")
    public String printEnodresementNotice() {

        objection = revisionPetitionService.findById(Long.valueOf(parameters.get("objectionId")[0]), false);

        final ReportOutput reportOutput = new ReportOutput();
        if (objection != null && objection.getObjectionNumber() != null) {
            final PtNotice ptNotice = noticeService.getPtNoticeByNoticeNumberAndNoticeType(objection
                    .getObjectionNumber().concat(PropertyTaxConstants.NOTICE_TYPE_REVISIONPETITION_ENDORSEMENT_PREFIX),
                    PropertyTaxConstants.NOTICE_TYPE_REVISIONPETITION_ENDORSEMENT);
            if (ptNotice != null) {
                final FileStoreMapper fsm = ptNotice.getFileStore();
                final File file = fileStoreService.fetch(fsm, FILESTORE_MODULE_NAME);
                byte[] bFile;
                try {
                    bFile = FileUtils.readFileToByteArray(file);
                } catch (final IOException e) {
                    e.printStackTrace();
                    throw new ApplicationRuntimeException("Exception while generating Hearing Notcie : " + e);
                }
                reportOutput.setReportOutputData(bFile);
                reportOutput.setReportFormat(FileFormat.PDF);
                reportId = ReportViewerUtil.addReportToSession(reportOutput, getSession());
            }
        } else
            addActionMessage(getText("objection.noendoresementNotice"));

        return NOTICE;
    }

    @ValidationErrorPage(value = "view")
    @Action(value = "/revPetition/revPetition-generateEnodresementNotice")
    public String generateEnodresementNotice() {
        ReportOutput reportOutput = new ReportOutput();
        InputStream endoresementPdf = null;

        /*
         * Change basic property status from Objected to Assessed.
         */
        Position position = null;
        User user = null;

        position = eisCommonService.getPositionByUserId(EgovThreadLocals.getUserId());
        user = userService.getUserById(EgovThreadLocals.getUserId());

        /*
         * Change workflow object as Active property and Active one to history.
         */
        if (objection.getGenerateSpecialNotice() != null && !objection.getGenerateSpecialNotice()) {
            objection.getBasicProperty().setStatus(
                    propertyStatusDAO.getPropertyStatusByCode(PropertyTaxConstants.STATUS_CODE_ASSESSED));
            objection.getBasicProperty().setUnderWorkflow(Boolean.FALSE);
            /*
             * Mean if commissioner reject revision petition, then current
             * workflow property will become history record.
             */
            objection.getProperty().setStatus(STATUS_ISHISTORY);

            objection.end().withStateValue(PropertyTaxConstants.WFLOW_ACTION_END).withOwner(position).withOwner(user)
                    .withComments(approverComments);

        } else
            updateStateAndStatus(objection); // If objection not rejected, then
        // print special notice.
        getSession().remove(ReportConstants.ATTRIB_EGOV_REPORT_OUTPUT_MAP);
        reportOutput = createEndoresement(reportOutput, objection);
        if (reportOutput != null && reportOutput.getReportOutputData() != null)
            endoresementPdf = new ByteArrayInputStream(reportOutput.getReportOutputData());
        noticeService.saveNotice(
                objection.getObjectionNumber(),
                objection.getObjectionNumber().concat(
                        PropertyTaxConstants.NOTICE_TYPE_REVISIONPETITION_ENDORSEMENT_PREFIX),
                PropertyTaxConstants.NOTICE_TYPE_REVISIONPETITION_ENDORSEMENT, objection.getBasicProperty(),
                endoresementPdf);

        revisionPetitionService.updateRevisionPetition(objection);

        addActionMessage(getText("objection.endoresementNotice.success"));
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("ObjectionAction | generateEnodresementNotice | End " + objection);

        if (objection != null && objection.getObjectionNumber() != null) {
            final PtNotice ptNotice = noticeService.getPtNoticeByNoticeNumberAndNoticeType(objection
                    .getObjectionNumber().concat(PropertyTaxConstants.NOTICE_TYPE_REVISIONPETITION_ENDORSEMENT_PREFIX),
                    PropertyTaxConstants.NOTICE_TYPE_REVISIONPETITION_ENDORSEMENT);
            if (ptNotice != null) {
                final FileStoreMapper fsm = ptNotice.getFileStore();
                final File file = fileStoreService.fetch(fsm, FILESTORE_MODULE_NAME);
                byte[] bFile;
                try {
                    bFile = FileUtils.readFileToByteArray(file);
                } catch (final IOException e) {
                    e.printStackTrace();
                    throw new ApplicationRuntimeException("Exception while generating Hearing Notcie : " + e);
                }
                reportOutput.setReportOutputData(bFile);
                reportOutput.setReportFormat(FileFormat.PDF);
                reportId = ReportViewerUtil.addReportToSession(reportOutput, getSession());
            }
        }
        return NOTICE;
    }

    @ValidationErrorPage(value = "view")
    @Action(value = "/revPetition/revPetition-generateSpecialNotice")
    public String generateSpecialNotice() {

        /*
         * Change basic property status from Objected to Assessed.
         */
        Position position = null;
        User user = null;

        position = eisCommonService.getPositionByUserId(EgovThreadLocals.getUserId());
        user = userService.getUserById(EgovThreadLocals.getUserId());
        /*
         * End workflow
         */
        if (objection.getGenerateSpecialNotice() != null && objection.getGenerateSpecialNotice()) {
            objection.getBasicProperty().setStatus(
                    propertyStatusDAO.getPropertyStatusByCode(PropertyTaxConstants.STATUS_CODE_ASSESSED));
            objection.getBasicProperty().getProperty().setStatus(STATUS_ISHISTORY);
            objection.getBasicProperty().setUnderWorkflow(Boolean.FALSE);
            objection.getProperty().setStatus(STATUS_ISACTIVE);

            objection.end().withStateValue(PropertyTaxConstants.WFLOW_ACTION_END).withOwner(position).withOwner(user)
                    .withComments(approverComments);
        }

        generateSpecialNotice(objection.getProperty(), (BasicPropertyImpl) objection.getBasicProperty());

        revisionPetitionService.updateRevisionPetition(objection);
        getSession().remove(ReportConstants.ATTRIB_EGOV_REPORT_OUTPUT_MAP);
        /* return STRUTS_RESULT_MESSAGE; */
        final ReportOutput reportOutput = new ReportOutput();
        if (objection != null && objection.getObjectionNumber() != null) {
            final PtNotice ptNotice = noticeService.getPtNoticeByNoticeNumberAndNoticeType(objection
                    .getObjectionNumber()
                    .concat(PropertyTaxConstants.NOTICE_TYPE_REVISIONPETITION_SPECIALNOTICE_PREFIX),
                    PropertyTaxConstants.NOTICE_TYPE_SPECIAL_NOTICE);
            if (ptNotice != null) {
                final FileStoreMapper fsm = ptNotice.getFileStore();
                final File file = fileStoreService.fetch(fsm, FILESTORE_MODULE_NAME);
                byte[] bFile;
                try {
                    bFile = FileUtils.readFileToByteArray(file);
                } catch (final IOException e) {
                    e.printStackTrace();
                    throw new ApplicationRuntimeException("Exception while generating Hearing Notcie : " + e);
                }
                reportOutput.setReportOutputData(bFile);
                reportOutput.setReportFormat(FileFormat.PDF);
                reportId = ReportViewerUtil.addReportToSession(reportOutput, getSession());
            }
        }
        return NOTICE;
    }

    /**
     * @param objection
     * @param applicationType
     */
    public void sendEmailandSms(final RevisionPetition objection, final String applicationType) {

        if (objection != null) {
            final User user = objection.getBasicProperty().getPrimaryOwner();
            final String mobileNumber = user.getMobileNumber();
            final String emailid = user.getEmailId();
            final String applicantName = user.getName();
            final List<String> args = new ArrayList<String>();
            args.add(applicantName);
            String smsMsg = "";
            String emailSubject = "";
            String emailBody = "";

            if (applicationType.equalsIgnoreCase(REVISION_PETITION_CREATED)) {

                args.add(objection.getObjectionNumber());
                args.add(sMSEmailService.getCityName());
                if (mobileNumber != null)
                    smsMsg = getText("msg.revPetitioncreate.sms", args);
                if (emailid != null) {
                    emailSubject = getText("msg.revPetitioncreate.email.subject");
                    emailBody = getText("msg.revPetitioncreate.email", args);
                }
            } else if (applicationType.equalsIgnoreCase(REVISION_PETITION_HEARINGNOTICEGENERATED)) {

                if (objection.getHearings() != null && objection.getHearings().size() > 0) {
                    args.add(DateUtils.getFormattedDate(objection.getHearings().get(0).getPlannedHearingDt(),
                            "dd/MM/yyyy"));
                    args.add(objection.getHearings().get(0).getHearingVenue());
                    args.add(objection.getHearings().get(0).getHearingTime());
                    args.add(sMSEmailService.getCityName());
                    if (mobileNumber != null)
                        smsMsg = getText("msg.revPetitionHearingNotice.sms", args);
                    if (emailid != null) {
                        emailSubject = getText("msg.revPetitionHearingNotice.email.subject");
                        emailBody = getText("msg.revPetitionHearingNotice.email", args);
                    }
                }
            } else if (applicationType.equalsIgnoreCase(REVISION_PETITION_ENDORESEMENTGENERATED)) {
                args.add(sMSEmailService.getCityName());
                if (mobileNumber != null)
                    smsMsg = getText("msg.revPetitionEndoresement.sms", args);
                if (emailid != null) {
                    emailSubject = getText("msg.revPetitionHearingNotice.email.subject");
                    emailBody = getText("msg.revPetitionEndoresement.email", args);
                }
            }
            if (mobileNumber != null)
                messagingService.sendSMS(mobileNumber, smsMsg);
            if (emailid != null)
                messagingService.sendEmail(emailid, emailSubject, emailBody);
        }
    }

    /**
     * @param property
     */
    private void setFloorDetails(final Property property) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Entered into setFloorDetails, Property: " + property);

        final List<Floor> floors = property.getPropertyDetail().getFloorDetails();
        objection.getBasicProperty().getProperty().getPropertyDetail().setFloorDetails(floors);
        if (null != objection.getProperty())
            objection.getProperty().getPropertyDetail().setFloorDetailsProxy(floors);

        int i = 0;
        for (final Floor flr : floors) {
            floorNoStr[i] = FLOOR_MAP.get(flr.getFloorNo());
            i++;
        }
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Exiting from setFloorDetails: ");
    }

    @Action(value = "/revPetition/revPetition-view")
    public String view() {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("ObjectionAction | view | Start");
        objection = revisionPetitionService.findById(Long.valueOf(parameters.get("objectionId")[0]), false);
        getPropertyView(objection.getBasicProperty().getUpicNo());

        if (objection != null && objection.getBasicProperty() != null
                && objection.getBasicProperty().getPropertyID() != null) {
            final PropertyID propertyID = objection.getBasicProperty().getPropertyID();
            northBoundary = propertyID.getNorthBoundary();
            southBoundary = propertyID.getSouthBoundary();
            eastBoundary = propertyID.getEastBoundary();
            westBoundary = propertyID.getWestBoundary();
        }
        populatePropertyTypeCategory();

        if (objection != null && objection.getProperty() != null) {
            setReasonForModify(objection.getProperty().getPropertyDetail().getPropertyMutationMaster().getCode());
            if (objection.getProperty().getPropertyDetail().getSitalArea() != null)
                setAreaOfPlot(objection.getProperty().getPropertyDetail().getSitalArea().getArea().toString());

            if (objection.getProperty().getPropertyDetail().getFloorDetails().size() > 0)
                setFloorDetails(objection.getProperty());
            if (objection.getProperty().getPropertyDetail().getPropertyTypeMaster() != null)
                propTypeObjId = objection.getProperty().getPropertyDetail().getPropertyTypeMaster().getId().toString();
        }

        setOwnerName(objection.getBasicProperty().getProperty());
        setPropertyAddress(objection.getBasicProperty().getAddress());

        propStatVal = propertyStatusValuesDAO.getLatestPropertyStatusValuesByPropertyIdAndreferenceNo(objection
                .getBasicProperty().getUpicNo(), objection.getObjectionNumber());

        // setupWorkflowDetails();
        if (objection != null && objection.getState() != null)
            setUpWorkFlowHistory(objection.getState().getId());
        setOwnerName(objection.getBasicProperty().getProperty());
        setPropertyAddress(objection.getBasicProperty().getAddress());
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("ObjectionAction | view | End");
        return "view";
    }

    public String viewObjectionDetails() {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("ObjectionAction | viewObjectionDetails | Start");
        objection = revisionPetitionService.find("from Objection where objectionNumber like ?",
                objection.getObjectionNumber());
        setOwnerName(objection.getBasicProperty().getProperty());
        setPropertyAddress(objection.getBasicProperty().getAddress());
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("ObjectionAction | viewObjectionDetails | End");
        return "viewDetails";
    }

    @Action(value = "/revPetition/revPetition-reject")
    public String rejectRevisionPetition() {

        updateStateAndStatus(objection);
        revisionPetitionService.updateRevisionPetition(objection);
        return STRUTS_RESULT_MESSAGE;
    }

    public String updateRecordObjection() {

        // objectionService.update(objection);
        revisionPetitionService.updateRevisionPetition(objection);

        updateStateAndStatus(objection);
        return STRUTS_RESULT_MESSAGE;
    }

    private void updateStateAndStatus(final RevisionPetition revPetition) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("ObjectionAction | updateStateAndStatus | Start");
        Position position = null;
        User user = null, loggedInUser = null;
        WorkFlowMatrix wfmatrix = null;
        user = userService.getUserById(EgovThreadLocals.getUserId());
        loggedInUser = user;

        if (null == objection.getState()) {
            if (loggedUserIsEmployee)
                wfmatrix = revisionPetitionWorkFlowService.getWfMatrix(objection.getStateType(), null, null,
                        getAdditionalRule(), PropertyTaxConstants.REVISIONPETITION_WF_REGISTERED, null);
            else
                wfmatrix = revisionPetitionWorkFlowService.getWfMatrix(objection.getStateType(), null, null,
                        getAdditionalRule(), PropertyTaxConstants.REVISIONPETITION_CREATED, null);
        } else
            wfmatrix = revisionPetitionWorkFlowService.getWfMatrix(objection.getStateType(), null, null,
                    getAdditionalRule(), objection.getCurrentState().getValue(), null);

        if (approverPositionId != null && approverPositionId != -1)
            position = positionMasterService.getPositionById(approverPositionId);

        if (revPetition.getState() == null) {
            // Get the default revenue cleark from admin boundary.
            if (position == null && (approverPositionId == null || approverPositionId != -1)) {
                final Assignment assignment = propService.getUserPositionByZone(objection.getBasicProperty());
                if (assignment != null)
                    position = assignment.getPosition();
            }
            updateRevisionPetitionStatus(wfmatrix, objection, PropertyTaxConstants.OBJECTION_CREATED);

            if (position != null)
                user = eisCommonService.getUserForPosition(position.getId(), new Date());

            objection.start().withNextAction(wfmatrix.getPendingActions()).withStateValue(wfmatrix.getCurrentState())
                    .withDateInfo(new DateTime().toDate()).withOwner(position).withSenderName(loggedInUser.getName())
                    .withOwner(user).withComments(approverComments);

            if (loggedUserIsEmployee && user != null)
                addActionMessage(getText("objection.forward", new String[] { user.getUsername() }));

            propService.updateIndexes(objection, PropertyTaxConstants.APPLICATION_TYPE_REVISION_PETITION);

        } else if (workFlowAction != null && !"".equals(workFlowAction)
                && !WFLOW_ACTION_STEP_SAVE.equalsIgnoreCase(workFlowAction)) {

            if (WFLOW_ACTION_STEP_REJECT.equalsIgnoreCase(workFlowAction)
                    || workFlowAction.equalsIgnoreCase("cancel unconsidered")) {

                wfmatrix = revisionPetitionWorkFlowService.getPreviousStateFromWfMatrix(objection.getStateType(), null,
                        null, getAdditionalRule(), objection.getCurrentState().getValue(), objection.getCurrentState()
                                .getNextAction());
                if (approverPositionId == null || approverPositionId != -1)
                    position = objection.getCurrentState().getOwnerPosition();
            }
            if (position == null)
                position = eisCommonService.getPositionByUserId(EgovThreadLocals.getUserId());

            if (wfmatrix != null)
                workFlowTransition(objection, workFlowAction, approverComments, wfmatrix, position, loggedInUser);
            // Update elastic search index on each workflow.
            propService.updateIndexes(objection, PropertyTaxConstants.APPLICATION_TYPE_REVISION_PETITION);

        } else if (workFlowAction != null && !"".equals(workFlowAction)
                && WFLOW_ACTION_STEP_SAVE.equalsIgnoreCase(workFlowAction))
            addActionMessage(getText("file.save"));

    }

    /**
     * @param objection
     * @param workFlowAction
     * @param comments
     * @param wfmatrix
     * @param position
     * @param loggedInUser
     */
    public void workFlowTransition(final RevisionPetition objection, final String workFlowAction,
            final String comments, WorkFlowMatrix wfmatrix, Position position, final User loggedInUser) {
        boolean positionFoundInHistory = false;
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("revisionpetitionaction ||Starting workFlowTransition method for objection");

        if (workFlowAction.equalsIgnoreCase("approve") || workFlowAction.equalsIgnoreCase("forward")
                || workFlowAction.equalsIgnoreCase("approve objection")
                || workFlowAction.equalsIgnoreCase("forward to approver")) {
            if (wfmatrix != null
                    && (wfmatrix.getNextStatus() != null
                            && wfmatrix.getNextStatus().equalsIgnoreCase(PropertyTaxConstants.OBJECTION_HEARING_FIXED)
                            || wfmatrix.getCurrentState().equalsIgnoreCase(
                                    PropertyTaxConstants.REVISIONPETITION_INSPECTIONVERIFIED) || wfmatrix
                            .getCurrentState().equalsIgnoreCase(PropertyTaxConstants.REVISIONPETITION_WF_REGISTERED))) {
                for (final StateHistory stateHistoryObj : objection.getState().getHistory()) {
                    if (stateHistoryObj.getValue().equalsIgnoreCase(PropertyTaxConstants.REVISIONPETITION_CREATED)) {
                        position = stateHistoryObj.getOwnerPosition();
                        final User sender = eisCommonService.getUserForPosition(position.getId(), new Date());
                        if (sender != null)
                            addActionMessage(getText("objection.forward", new String[] { sender.getUsername() }));

                        positionFoundInHistory = true;
                        break;
                    }
                    if (stateHistoryObj.getValue()
                            .equalsIgnoreCase(PropertyTaxConstants.REVISIONPETITION_WF_REGISTERED)) {
                        position = getWorkFlowInitiator(objection, position);

                        // First time when commisioner forwarding record from
                        // Ulb operator, then only we need to change status.
                        if (objection.getEgwStatus() != null
                                && objection.getEgwStatus().getCode()
                                        .equalsIgnoreCase(PropertyTaxConstants.OBJECTION_CREATED))
                            updateRevisionPetitionStatus(wfmatrix, objection,
                                    PropertyTaxConstants.OBJECTION_HEARING_FIXED);

                        positionFoundInHistory = true;
                        break;
                    }
                }
                if (!positionFoundInHistory && objection.getState() != null)
                    if (objection.getState().getValue().equalsIgnoreCase(PropertyTaxConstants.REVISIONPETITION_CREATED)
                            || objection.getState().getValue()
                                    .equalsIgnoreCase(PropertyTaxConstants.REVISIONPETITION_WF_REGISTERED)) {
                        positionFoundInHistory = true;
                        updateRevisionPetitionStatus(wfmatrix, objection, PropertyTaxConstants.OBJECTION_HEARING_FIXED);
                        position = getWorkFlowInitiator(objection, position);
                    }
            }
            // loggedInUser =
            // eisCommonService.getUserForPosition(position.getId(), new
            // Date());

            objection.transition(true).withStateValue(wfmatrix.getNextState()).withOwner(position)
                    .withSenderName(loggedInUser.getName()).withDateInfo(new DateTime().toDate())
                    .withNextAction(wfmatrix.getNextAction()).withComments(approverComments);

            if (wfmatrix.getNextAction() != null && wfmatrix.getNextAction().equalsIgnoreCase("END"))
                objection.end().withStateValue(wfmatrix.getNextState())
                        .withOwner(objection.getCurrentState().getOwnerPosition())
                        .withSenderName(loggedInUser.getName()).withNextAction(wfmatrix.getNextAction())
                        .withDateInfo(new DateTime().toDate()).withComments(approverComments);

            if (wfmatrix.getNextStatus() != null)
                updateRevisionPetitionStatus(wfmatrix, objection, null);
            if (approverName != null && !approverName.isEmpty() && !approverName.equalsIgnoreCase("----Choose----"))
                addActionMessage(getText("objection.forward", new String[] { approverName }));
            else if (loggedInUser != null && !positionFoundInHistory)
                addActionMessage(getText("objection.forward", new String[] { loggedInUser.getUsername() }));
            // objectionWorkflowService.transition(workFlowAction.toLowerCase(),objection,
            // comments);
        } else if (workFlowAction.equalsIgnoreCase("Reject Inspection")) {
            final List<StateHistory> stateHistoryList = objection.getState().getHistory();
            for (final StateHistory stateHistoryObj : stateHistoryList)
                if (stateHistoryObj.getValue().equalsIgnoreCase(PropertyTaxConstants.REVISIONPETITION_HEARINGCOMPLETED)) {
                    position = stateHistoryObj.getOwnerPosition();
                    break;
                }
            wfmatrix = revisionPetitionWorkFlowService.getWfMatrix(objection.getStateType(), null, null,
                    getAdditionalRule(), PropertyTaxConstants.WF_STATE_REJECTED, null);

            objection.setEgwStatus(egwStatusDAO.getStatusByModuleAndCode(PropertyTaxConstants.OBJECTION_MODULE,
                    PropertyTaxConstants.OBJECTION_HEARING_COMPLETED));

            if (position != null) {
                objection.transition(true).withNextAction(wfmatrix.getNextAction())
                        .withStateValue(PropertyTaxConstants.WF_STATE_REJECTED).withOwner(position)
                        .withSenderName(loggedInUser.getName()).withDateInfo(new DateTime().toDate())
                        .withComments(approverComments);

                final Assignment assignment = assignmentService.getPrimaryAssignmentForPositionAndDate(
                        position.getId(), new Date());
                if (assignment != null)
                    addActionMessage(getText("objection.forward",
                            new String[] { assignment.getEmployee().getUsername() }));
            }

        } else if (workFlowAction.equalsIgnoreCase("Reject") || workFlowAction.equalsIgnoreCase("reject")) {
            final List<StateHistory> stateHistoryList = objection.getState().getHistory();
            for (final StateHistory stateHistoryObj : stateHistoryList)
                if (stateHistoryObj.getValue().equalsIgnoreCase(objection.getCurrentState().getValue())) {
                    position = stateHistoryObj.getOwnerPosition();
                    break;
                }

            if (objection.getCurrentState() != null
                    && (objection.getCurrentState().getValue().equalsIgnoreCase(REJECTED) || objection
                            .getCurrentState().getValue()
                            .equalsIgnoreCase(PropertyTaxConstants.REVISIONPETITION_CREATED))) {
                objection.end().withStateValue(wfmatrix.getNextState()).withOwner(position)
                        .withSenderName(loggedInUser.getName()).withNextAction(wfmatrix.getNextAction())
                        .withDateInfo(new DateTime().toDate()).withComments(approverComments);

                updateRevisionPetitionStatus(wfmatrix, objection, REJECTED);

            } else {// ASSUMPTION HERE IS WE ALREADY HAVE PREVIOUS WF MATRIX
                // DATA.
                objection.transition(true).withStateValue(wfmatrix.getCurrentState()).withOwner(position)
                        .withSenderName(loggedInUser.getName()).withDateInfo(new DateTime().toDate())
                        .withNextAction(wfmatrix.getPendingActions()).withComments(approverComments);

                if (workFlowAction.equalsIgnoreCase("Reject"))
                    updateRevisionPetitionStatus(wfmatrix, objection, null);
            }

            if (approverName != null && !approverName.isEmpty() && !approverName.equalsIgnoreCase("----Choose----"))
                addActionMessage(getText("objection.forward", new String[] { approverName }));
            else if (loggedInUser != null)
                addActionMessage(getText("objection.forward", new String[] { loggedInUser.getUsername() }));

        } else if (workFlowAction.equalsIgnoreCase("Print Endoresement")) {
            objection.transition(true).withStateValue(wfmatrix.getNextState()).withOwner(position)
                    .withSenderName(loggedInUser.getName()).withDateInfo(new DateTime().toDate())
                    .withNextAction(wfmatrix.getNextAction()).withComments(approverComments);
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("revisionpetitionaction ||ended  workflow for objection");

        }
        revisionPetitionService.applyAuditing(objection.getState());
    }

    /**
     * @param objection
     * @param position
     * @return
     */
    private Position getWorkFlowInitiator(final RevisionPetition objection, Position position) {
        final Assignment assignment = assignmentService.getPrimaryAssignmentForUser(objection.getCreatedBy().getId());
        if (assignment != null) {
            position = assignment.getPosition();
            addActionMessage(getText("objection.forward", new String[] { assignment.getEmployee().getName() }));
        }
        return position;
    }

    private void updateRevisionPetitionStatus(final WorkFlowMatrix wfmatrix, final RevisionPetition objection,
            final String status) {

        EgwStatus egwStatus = null;
        if (status != null && !"".equals(status))
            egwStatus = egwStatusDAO.getStatusByModuleAndCode(PropertyTaxConstants.OBJECTION_MODULE, status);

        else if (wfmatrix != null && wfmatrix.getNextStatus() != null && objection != null)
            egwStatus = egwStatusDAO.getStatusByModuleAndCode(PropertyTaxConstants.OBJECTION_MODULE,
                    wfmatrix.getNextStatus());
        if (egwStatus != null)
            objection.setEgwStatus(egwStatus);

    }

    private void modifyBasicProp() {
        Date propCompletionDate = null;
        final Long oldPropTypeId = objection.getProperty().getPropertyDetail().getPropertyTypeMaster().getId();

        if (propTypeObjId != null && !propTypeObjId.trim().isEmpty() && !propTypeObjId.equals("-1")) {
            final PropertyTypeMaster propTypeMstr = (PropertyTypeMaster) getPersistenceService().find(
                    "from PropertyTypeMaster ptm where ptm.id = ?", Long.valueOf(propTypeObjId));

            if (!propTypeMstr.getCode().equalsIgnoreCase(OWNERSHIP_TYPE_VAC_LAND))
                propCompletionDate = propService.getLowestDtOfCompFloorWise(objection.getProperty().getPropertyDetail()
                        .getFloorDetailsProxy());
            else
                propCompletionDate = objection.getProperty().getPropertyDetail().getDateOfCompletion();

        }
        if (propCompletionDate != null) {
            objection.getBasicProperty().setPropOccupationDate(propCompletionDate);
            objection.getProperty().setEffectiveDate(propCompletionDate);
        }

        propService.createProperty(objection.getProperty(), getAreaOfPlot() != null ? getAreaOfPlot() : "",
                reasonForModify, propTypeObjId != null ? propTypeObjId : null, objection.getProperty()
                        .getPropertyDetail().getPropertyUsage() != null ? objection.getProperty().getPropertyDetail()
                        .getPropertyUsage().getId().toString() : null, objection.getProperty().getPropertyDetail()
                        .getPropertyOccupation() != null ? objection.getProperty().getPropertyDetail()
                        .getPropertyOccupation().getId().toString() : null, STATUS_WORKFLOW, objection.getProperty()
                        .getDocNumber(), "",
                objection.getProperty().getPropertyDetail().getFloorType() != null ? objection.getProperty()
                        .getPropertyDetail().getFloorType().getId() : null, objection.getProperty().getPropertyDetail()
                        .getRoofType() != null ? objection.getProperty().getPropertyDetail().getRoofType().getId()
                        : null, objection.getProperty().getPropertyDetail().getWallType() != null ? objection
                        .getProperty().getPropertyDetail().getWallType().getId() : null, objection.getProperty()
                        .getPropertyDetail().getWoodType() != null ? objection.getProperty().getPropertyDetail()
                        .getWoodType().getId() : null, taxExemptedReason);

        updatePropertyID(objection.getBasicProperty());
        final PropertyTypeMaster propTypeMstr = (PropertyTypeMaster) getPersistenceService().find(
                "from PropertyTypeMaster ptm where ptm.code = ?", OWNERSHIP_TYPE_VAC_LAND);

        if (oldPropTypeId == propTypeMstr.getId() && Long.parseLong(propTypeObjId) != propTypeMstr.getId()
                || oldPropTypeId != propTypeMstr.getId() && Long.parseLong(propTypeObjId) == propTypeMstr.getId())
            if (propTypeMstr != null
                    && org.apache.commons.lang.StringUtils.equals(propTypeMstr.getId().toString(), propTypeObjId))
                propService.changePropertyDetail(objection.getProperty(), new VacantProperty(), 0);
            else
                propService.changePropertyDetail(objection.getProperty(), new BuiltUpProperty(), objection
                        .getProperty().getPropertyDetail().getFloorDetails().size());

        propService.modifyDemand(objection.getProperty(), (PropertyImpl) objection.getBasicProperty().getProperty());

    }

    /**
     * @param propertyId
     */
    private void getPropertyView(final String propertyId) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("ObjectionAction | getPropertyView | Start");
        viewPropertyAction.setPersistenceService(persistenceService);
        viewPropertyAction.setBasicPropertyDAO(basicPropertyDAO);
        viewPropertyAction.setPtDemandDAO(ptDemandDAO);
        viewPropertyAction.setPropertyId(propertyId);
        viewPropertyAction.setPropertyTaxUtil(new PropertyTaxUtil());
        viewPropertyAction.setUserService(userService);
        viewPropertyAction.setSession(getSession());
        viewPropertyAction.viewForm();
        objection.setBasicProperty(viewPropertyAction.getBasicProperty());
        viewMap = viewPropertyAction.getViewMap();
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("ObjectionAction | getPropertyView | End");
    }

    public void vaidatePropertyDetails() {
        if (reasonForModify == null || reasonForModify.equals("-1"))
            addActionError(getText("mandatory.rsnForMdfy"));
        validateProperty(
                objection.getProperty(),
                getAreaOfPlot() != null ? getAreaOfPlot() : "",
                objection.getProperty().getPropertyDetail().getDateOfCompletion() != null ? dateformat.format(
                        objection.getProperty().getPropertyDetail().getDateOfCompletion()).toString() : "",
                eastBoundary, westBoundary, southBoundary, northBoundary, propTypeObjId != null ? propTypeObjId : null,
                ownerName, ownerName, objection.getProperty().getPropertyDetail().getFloorType() != null ? objection
                        .getProperty().getPropertyDetail().getFloorType().getId() : null, objection.getProperty()
                        .getPropertyDetail().getRoofType() != null ? objection.getProperty().getPropertyDetail()
                        .getRoofType().getId() : null,
                objection.getProperty().getPropertyDetail().getWallType() != null ? objection.getProperty()
                        .getPropertyDetail().getWallType().getId() : null, objection.getProperty().getPropertyDetail()
                        .getWoodType() != null ? objection.getProperty().getPropertyDetail().getWoodType().getId()
                        : null);

    }

    private void populatePropertyTypeCategory() {
        PropertyTypeMaster propTypeMstr = null;
        if (propTypeObjId != null && !propTypeObjId.trim().isEmpty() && !propTypeObjId.equals("-1"))
            propTypeMstr = (PropertyTypeMaster) getPersistenceService().find(
                    "from PropertyTypeMaster ptm where ptm.id = ?", Long.valueOf(propTypeObjId));
        else if (objection != null && objection.getProperty() != null
                && objection.getProperty().getPropertyDetail() != null
                && objection.getProperty().getPropertyDetail().getPropertyTypeMaster() != null
                && !objection.getProperty().getPropertyDetail().getPropertyTypeMaster().getId().equals(-1))
            propTypeMstr = objection.getProperty().getPropertyDetail().getPropertyTypeMaster();
        else if (objection.getBasicProperty() != null)
            propTypeMstr = objection.getBasicProperty().getProperty().getPropertyDetail().getPropertyTypeMaster();

        if (propTypeMstr != null) {
            if (propTypeMstr.getCode().equalsIgnoreCase(OWNERSHIP_TYPE_VAC_LAND))
                setPropTypeCategoryMap(VAC_LAND_PROPERTY_TYPE_CATEGORY);
            else
                setPropTypeCategoryMap(NON_VAC_LAND_PROPERTY_TYPE_CATEGORY);
        } else
            setPropTypeCategoryMap(Collections.emptyMap());
    }

    public List<Floor> getFloorDetails() {
        return new ArrayList<Floor>(objection.getBasicProperty().getProperty().getPropertyDetail().getFloorDetails());
    }

    public Map<String, String> getPropTypeCategoryMap() {
        return propTypeCategoryMap;
    }

    public void setPropTypeCategoryMap(final Map<String, String> propTypeCategoryMap) {
        this.propTypeCategoryMap = propTypeCategoryMap;
    }

    public RevisionPetition getObjection() {
        return objection;
    }

    public void setObjection(final RevisionPetition objection) {
        this.objection = objection;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(final String propertyId) {
        this.propertyId = propertyId;
    }

    public Map<String, Object> getViewMap() {
        return viewMap;
    }

    public RevisionPetitionService getRevisionPetitionService() {
        return revisionPetitionService;
    }

    private void updatePropertyID(final BasicProperty basicProperty) {
        final PropertyID propertyId = basicProperty.getPropertyID();
        if (propertyId != null) {
            propertyId.setEastBoundary(getEastBoundary());
            propertyId.setWestBoundary(getWestBoundary());
            propertyId.setNorthBoundary(getNorthBoundary());
            propertyId.setSouthBoundary(getSouthBoundary());
        }
    }

    public void setRevisionPetitionService(final RevisionPetitionService revisionPetitionService) {
        this.revisionPetitionService = revisionPetitionService;
    }

    public PersistenceService<Property, Long> getPropertyImplService() {
        return propertyImplService;
    }

    public void setPropertyImplService(final PersistenceService<Property, Long> propertyImplService) {
        this.propertyImplService = propertyImplService;
    }

    public void setViewPropertyAction(final ViewPropertyAction viewPropertyAction) {
        this.viewPropertyAction = viewPropertyAction;
    }

    public void setObjectionWorkflowService(final WorkflowService<RevisionPetition> objectionWorkflowService) {
        this.objectionWorkflowService = objectionWorkflowService;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(final Property property) {
        if (property != null)
            ownerName = property.getBasicProperty().getFullOwnerName();
    }

    public String getPropertyAddress() {
        return propertyAddress;
    }

    public void setPropertyAddress(final Address address) {
        if (address != null)
            propertyAddress = address.toString();
    }

    public void setPropService(final PropertyService propService) {
        this.propService = propService;
    }

    public boolean getIsShowAckMessage() {
        return isShowAckMessage;
    }

    public void setIsShowAckMessage(final boolean isShowAckMessage) {
        this.isShowAckMessage = isShowAckMessage;
    }

    public void setBasicPropertyDAO(final BasicPropertyDAO basicPropertyDAO) {
        this.basicPropertyDAO = basicPropertyDAO;
    }

    public void setUserService(final UserService userService) {
        this.userService = userService;
    }

    public PropertyStatusValues getPropStatVal() {
        return propStatVal;
    }

    public void setPropStatVal(final PropertyStatusValues propStatVal) {
        this.propStatVal = propStatVal;
    }

    public String getReasonForModify() {
        return reasonForModify;
    }

    public void setReasonForModify(final String reasonForModify) {
        this.reasonForModify = reasonForModify;
    }

    public TreeMap<Integer, String> getFloorNoMap() {
        return floorNoMap;
    }

    public void setFloorNoMap(final TreeMap<Integer, String> floorNoMap) {
        this.floorNoMap = floorNoMap;
    }

    public String getPropTypeObjId() {
        return propTypeObjId;
    }

    public void setPropTypeObjId(final String propTypeObjId) {
        this.propTypeObjId = propTypeObjId;
    }

    public Integer getReportId() {
        return reportId;
    }

    public void setReportId(final Integer reportId) {
        this.reportId = reportId;
    }

    public Map<String, String> getDeviationPercentageMap() {
        return deviationPercentageMap;
    }

    public void setDeviationPercentageMap(final Map<String, String> deviationPercentageMap) {
        this.deviationPercentageMap = deviationPercentageMap;
    }

    public String[] getFloorNoStr() {
        return floorNoStr;
    }

    public void setFloorNoStr(final String[] floorNoStr) {
        this.floorNoStr = floorNoStr;
    }

    public List<DocumentType> getDocumentTypes() {
        return documentTypes;
    }

    public void setDocumentTypes(final List<DocumentType> documentTypes) {
        this.documentTypes = documentTypes;
    }

    public TreeMap<String, String> getHearingTimingMap() {
        return hearingTimingMap;
    }

    public void setHearingTimingMap(final TreeMap<String, String> hearingTimingMap) {
        this.hearingTimingMap = hearingTimingMap;
    }

    public String getNorthBoundary() {
        return northBoundary;
    }

    public void setNorthBoundary(final String northBoundary) {
        this.northBoundary = northBoundary;
    }

    public String getSouthBoundary() {
        return southBoundary;
    }

    public void setSouthBoundary(final String southBoundary) {
        this.southBoundary = southBoundary;
    }

    public String getEastBoundary() {
        return eastBoundary;
    }

    public void setEastBoundary(final String eastBoundary) {
        this.eastBoundary = eastBoundary;
    }

    public String getWestBoundary() {
        return westBoundary;
    }

    public void setWestBoundary(final String westBoundary) {
        this.westBoundary = westBoundary;
    }

    public SimpleWorkflowService<RevisionPetition> getRevisionPetitionWorkFlowService() {
        return revisionPetitionWorkFlowService;
    }

    public void setRevisionPetitionWorkFlowService(
            final SimpleWorkflowService<RevisionPetition> revisionPetitionWorkFlowService) {
        this.revisionPetitionWorkFlowService = revisionPetitionWorkFlowService;
    }

    public PropertyTaxNumberGenerator getPropertyTaxNumberGenerator() {
        return propertyTaxNumberGenerator;
    }

    public void setPropertyTaxNumberGenerator(final PropertyTaxNumberGenerator propertyTaxNumberGenerator) {
        this.propertyTaxNumberGenerator = propertyTaxNumberGenerator;
    }

    @Override
    public SMSEmailService getsMSEmailService() {
        return sMSEmailService;
    }

    @Override
    public void setsMSEmailService(final SMSEmailService sMSEmailService) {
        this.sMSEmailService = sMSEmailService;
    }

    public Long getTaxExemptedReason() {
        return taxExemptedReason;
    }

    public void setTaxExemptedReason(final Long taxExemptedReason) {
        this.taxExemptedReason = taxExemptedReason;
    }

    public Boolean getLoggedUserIsEmployee() {
        return loggedUserIsEmployee;
    }

    public void setLoggedUserIsEmployee(final Boolean loggedUserIsEmployee) {
        this.loggedUserIsEmployee = loggedUserIsEmployee;
    }

    public String getAreaOfPlot() {
        return areaOfPlot;
    }

    public void setAreaOfPlot(final String areaOfPlot) {
        this.areaOfPlot = areaOfPlot;
    }

}
