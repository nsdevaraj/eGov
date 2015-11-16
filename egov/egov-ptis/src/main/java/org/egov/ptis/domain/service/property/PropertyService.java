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
 *      1) All versions of this program, verbatim or modified must carry this
 *         Legal Notice.
 *
 *      2) Any misrepresentation of the origin of the material is prohibited. It
 *         is required that all modified versions of this material be marked in
 *         reasonable ways as different from the original version.
 *
 *      3) This license does not grant any rights to any user of the program
 *         with regards to rights under trademark law for use of the trade names
 *         or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org
 ******************************************************************************/
package org.egov.ptis.domain.service.property;

import static java.lang.Boolean.FALSE;
import static java.math.BigDecimal.ZERO;
import static org.egov.ptis.constants.PropertyTaxConstants.APPLICATION_TYPE_ALTER_ASSESSENT;
import static org.egov.ptis.constants.PropertyTaxConstants.APPLICATION_TYPE_BIFURCATE_ASSESSENT;
import static org.egov.ptis.constants.PropertyTaxConstants.APPLICATION_TYPE_NEW_ASSESSENT;
import static org.egov.ptis.constants.PropertyTaxConstants.APPLICATION_TYPE_REVISION_PETITION;
import static org.egov.ptis.constants.PropertyTaxConstants.APPLICATION_TYPE_TRANSFER_OF_OWNERSHIP;
import static org.egov.ptis.constants.PropertyTaxConstants.ARR_COLL_STR;
import static org.egov.ptis.constants.PropertyTaxConstants.ARR_DMD_STR;
import static org.egov.ptis.constants.PropertyTaxConstants.BUILT_UP_PROPERTY;
import static org.egov.ptis.constants.PropertyTaxConstants.CURR_COLL_STR;
import static org.egov.ptis.constants.PropertyTaxConstants.CURR_DMD_STR;
import static org.egov.ptis.constants.PropertyTaxConstants.DEMANDRSN_CODE_CHQ_BOUNCE_PENALTY;
import static org.egov.ptis.constants.PropertyTaxConstants.DEMANDRSN_CODE_EDUCATIONAL_CESS;
import static org.egov.ptis.constants.PropertyTaxConstants.DEMANDRSN_CODE_GENERAL_TAX;
import static org.egov.ptis.constants.PropertyTaxConstants.DEMANDRSN_CODE_LIBRARY_CESS;
import static org.egov.ptis.constants.PropertyTaxConstants.DEMANDRSN_CODE_PENALTY_FINES;
import static org.egov.ptis.constants.PropertyTaxConstants.DEMANDRSN_CODE_SEWERAGE_TAX;
import static org.egov.ptis.constants.PropertyTaxConstants.DEMANDRSN_CODE_UNAUTHORIZED_PENALTY;
import static org.egov.ptis.constants.PropertyTaxConstants.DEMANDRSN_CODE_VACANT_TAX;
import static org.egov.ptis.constants.PropertyTaxConstants.DEMAND_RSNS_LIST;
import static org.egov.ptis.constants.PropertyTaxConstants.FILESTORE_MODULE_NAME;
import static org.egov.ptis.constants.PropertyTaxConstants.FLOOR_MAP;
import static org.egov.ptis.constants.PropertyTaxConstants.JUNIOR_ASSISTANT;
import static org.egov.ptis.constants.PropertyTaxConstants.OPEN_PLOT_UNIT_FLOORNUMBER;
import static org.egov.ptis.constants.PropertyTaxConstants.OWNERSHIP_TYPE_VAC_LAND;
import static org.egov.ptis.constants.PropertyTaxConstants.PROPERTYTAX_ROLEFORNONEMPLOYEE;
import static org.egov.ptis.constants.PropertyTaxConstants.PROPERTYTAX_WORKFLOWDEPARTEMENT;
import static org.egov.ptis.constants.PropertyTaxConstants.PROPERTYTAX_WORKFLOWDESIGNATION;
import static org.egov.ptis.constants.PropertyTaxConstants.PROPERTY_IS_DEFAULT;
import static org.egov.ptis.constants.PropertyTaxConstants.PROPERTY_MODIFY_REASON_ADD_OR_ALTER;
import static org.egov.ptis.constants.PropertyTaxConstants.PROPERTY_MODIFY_REASON_AMALG;
import static org.egov.ptis.constants.PropertyTaxConstants.PROPERTY_MODIFY_REASON_BIFURCATE;
import static org.egov.ptis.constants.PropertyTaxConstants.PROPERTY_MODIFY_REASON_DATA_ENTRY;
import static org.egov.ptis.constants.PropertyTaxConstants.PROPERTY_STATUS_MARK_DEACTIVE;
import static org.egov.ptis.constants.PropertyTaxConstants.PROP_CREATE_RSN;
import static org.egov.ptis.constants.PropertyTaxConstants.PROP_CREATE_RSN_BIFUR;
import static org.egov.ptis.constants.PropertyTaxConstants.PROP_SOURCE;
import static org.egov.ptis.constants.PropertyTaxConstants.PTMODULENAME;
import static org.egov.ptis.constants.PropertyTaxConstants.QUERY_PROPSTATVALUE_BY_UPICNO_CODE_ISACTIVE;
import static org.egov.ptis.constants.PropertyTaxConstants.SENIOR_ASSISTANT;
import static org.egov.ptis.constants.PropertyTaxConstants.SQUARE_YARD_TO_SQUARE_METER_VALUE;
import static org.egov.ptis.constants.PropertyTaxConstants.STATUS_CANCELLED;
import static org.egov.ptis.constants.PropertyTaxConstants.STATUS_WORKFLOW;
import static org.egov.ptis.constants.PropertyTaxConstants.VACANT_PROPERTY;
import static org.egov.ptis.constants.PropertyTaxConstants.WFLOW_ACTION_NAME_MODIFY;
import static org.egov.ptis.constants.PropertyTaxConstants.WF_STATE_APPROVAL_PENDING;
import static org.egov.ptis.constants.PropertyTaxConstants.MEESEVA_OPERATOR_ROLE;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.egov.commons.Area;
import org.egov.commons.Installment;
import org.egov.commons.dao.InstallmentDao;
import org.egov.demand.model.EgDemandDetails;
import org.egov.demand.model.EgDemandReason;
import org.egov.demand.model.EgDemandReasonMaster;
import org.egov.eis.entity.Assignment;
import org.egov.eis.service.AssignmentService;
import org.egov.eis.service.DesignationService;
import org.egov.eis.service.EmployeeService;
import org.egov.infra.admin.master.entity.AppConfigValues;
import org.egov.infra.admin.master.entity.Department;
import org.egov.infra.admin.master.entity.Module;
import org.egov.infra.admin.master.entity.Role;
import org.egov.infra.admin.master.entity.User;
import org.egov.infra.admin.master.service.AppConfigValueService;
import org.egov.infra.admin.master.service.DepartmentService;
import org.egov.infra.admin.master.service.ModuleService;
import org.egov.infra.admin.master.service.UserService;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.filestore.service.FileStoreService;
import org.egov.infra.rest.client.SimpleRestClient;
import org.egov.infra.search.elastic.entity.ApplicationIndex;
import org.egov.infra.search.elastic.entity.ApplicationIndexBuilder;
import org.egov.infra.search.elastic.service.ApplicationIndexService;
import org.egov.infra.utils.ApplicationNumberGenerator;
import org.egov.infra.utils.EgovThreadLocals;
import org.egov.infra.web.utils.WebUtils;
import org.egov.infra.workflow.entity.StateAware;
import org.egov.infstr.services.PersistenceService;
import org.egov.pims.commons.Position;
import org.egov.pims.commons.service.EisCommonsService;
import org.egov.ptis.client.model.calculator.APTaxCalculationInfo;
import org.egov.ptis.client.service.calculator.APTaxCalculator;
import org.egov.ptis.client.util.PropertyTaxUtil;
import org.egov.ptis.constants.PropertyTaxConstants;
import org.egov.ptis.domain.dao.demand.PtDemandDao;
import org.egov.ptis.domain.dao.property.BasicPropertyDAO;
import org.egov.ptis.domain.dao.property.PropertyStatusValuesDAO;
import org.egov.ptis.domain.entity.demand.FloorwiseDemandCalculations;
import org.egov.ptis.domain.entity.demand.PTDemandCalculations;
import org.egov.ptis.domain.entity.demand.Ptdemand;
import org.egov.ptis.domain.entity.enums.TransactionType;
import org.egov.ptis.domain.entity.objection.RevisionPetition;
import org.egov.ptis.domain.entity.property.Apartment;
import org.egov.ptis.domain.entity.property.BasicProperty;
import org.egov.ptis.domain.entity.property.Document;
import org.egov.ptis.domain.entity.property.DocumentType;
import org.egov.ptis.domain.entity.property.Floor;
import org.egov.ptis.domain.entity.property.FloorType;
import org.egov.ptis.domain.entity.property.Property;
import org.egov.ptis.domain.entity.property.PropertyDetail;
import org.egov.ptis.domain.entity.property.PropertyImpl;
import org.egov.ptis.domain.entity.property.PropertyMaterlizeView;
import org.egov.ptis.domain.entity.property.PropertyMutation;
import org.egov.ptis.domain.entity.property.PropertyMutationMaster;
import org.egov.ptis.domain.entity.property.PropertyOccupation;
import org.egov.ptis.domain.entity.property.PropertySource;
import org.egov.ptis.domain.entity.property.PropertyStatus;
import org.egov.ptis.domain.entity.property.PropertyStatusValues;
import org.egov.ptis.domain.entity.property.PropertyTypeMaster;
import org.egov.ptis.domain.entity.property.PropertyUsage;
import org.egov.ptis.domain.entity.property.RoofType;
import org.egov.ptis.domain.entity.property.StructureClassification;
import org.egov.ptis.domain.entity.property.TaxExeptionReason;
import org.egov.ptis.domain.entity.property.WallType;
import org.egov.ptis.domain.entity.property.WoodType;
import org.egov.ptis.domain.model.calculator.MiscellaneousTax;
import org.egov.ptis.domain.model.calculator.MiscellaneousTaxDetail;
import org.egov.ptis.domain.model.calculator.TaxCalculationInfo;
import org.egov.ptis.domain.model.calculator.UnitTaxCalculationInfo;
import org.egov.ptis.service.collection.PropertyTaxCollection;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class to perform services related to an Assessment
 *
 * @author subhash
 */
@Transactional(readOnly = true)
public class PropertyService {
    private static final Logger LOGGER = Logger.getLogger(PropertyService.class);
    private static final String WTMS_TAXDUE_RESTURL = "%s/wtms/rest/watertax/due/byptno/%s";
    private static final String PROPERTY_WORKFLOW_STARTED = "Property Workflow Started";
    private PersistenceService propPerServ;
    private Installment currentInstall;
    final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
    protected PersistenceService<BasicProperty, Long> basicPropertyService;
    private final Map<Installment, Set<EgDemandDetails>> demandDetails = new HashMap<Installment, Set<EgDemandDetails>>();
    private Map<Installment, Map<String, BigDecimal>> excessCollAmtMap = new LinkedHashMap<Installment, Map<String, BigDecimal>>();
    @Autowired
    private APTaxCalculator taxCalculator;
    private HashMap<Installment, TaxCalculationInfo> instTaxMap;
    @Autowired
    private PropertyTaxUtil propertyTaxUtil;
    @Autowired
    protected EisCommonsService eisCommonsService;
    @Autowired
    private ModuleService moduleDao;
    @Autowired
    private InstallmentDao installmentDao;
    @Autowired
    private UserService userService;
    @Autowired
    private ApplicationNumberGenerator applicationNumberGenerator;
    @Autowired
    @Qualifier("documentTypePersistenceService")
    private PersistenceService<DocumentType, Long> documentTypePersistenceService;
    @Autowired
    @Qualifier("fileStoreService")
    private FileStoreService fileStoreService;
    @Autowired
    private ApplicationIndexService applicationIndexService;
    @Autowired
    private SimpleRestClient simpleRestClient;
    @Autowired
    private PtDemandDao ptDemandDAO;
    @Autowired
    private BasicPropertyDAO basicPropertyDAO;
    @Autowired
    private PropertyStatusValuesDAO propertyStatusValuesDAO;
    @Autowired
    private AppConfigValueService appConfigValuesService;
    @Autowired
    private DesignationService designationService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    protected AssignmentService assignmentService;
    @Autowired
    private PropertyTaxCollection propertyTaxCollection;

    private BigDecimal totalAlv = BigDecimal.ZERO;

	/**
     * Creates a new property if property is in transient state else updates
     * persisted property
     *
     * @param property
     * @param areaOfPlot
     * @param mutationCode
     * @param propTypeId
     * @param propUsageId
     * @param propOccId
     * @param status
     * @param docnumber
     * @param nonResPlotArea
     * @param floorTypeId
     * @param roofTypeId
     * @param wallTypeId
     * @param woodTypeId
     * @param taxExemptId
     * @return Created or Updated property
     */
    public PropertyImpl createProperty(final PropertyImpl property, final String areaOfPlot, final String mutationCode,
            final String propTypeId, final String propUsageId, final String propOccId, final Character status,
            final String docnumber, final String nonResPlotArea, final Long floorTypeId, final Long roofTypeId,
            final Long wallTypeId, final Long woodTypeId, final Long taxExemptId) {
        LOGGER.debug("Entered into createProperty");
        LOGGER.debug("createProperty: Property: " + property + ", areaOfPlot: " + areaOfPlot + ", mutationCode: "
                + mutationCode + ",propTypeId: " + propTypeId + ", propUsageId: " + propUsageId + ", propOccId: "
                + propOccId + ", status: " + status);
        currentInstall = (Installment) getPropPerServ().find(
                "from Installment I where I.module.name=? and (I.fromDate <= ? and I.toDate >= ?) ", PTMODULENAME,
                new Date(), new Date());
        final PropertySource propertySource = (PropertySource) getPropPerServ().find(
                "from PropertySource where propSrcCode = ?", PROP_SOURCE);
        if (floorTypeId != null && floorTypeId != -1) {
            final FloorType floorType = (FloorType) getPropPerServ().find("From FloorType where id = ?", floorTypeId);
            property.getPropertyDetail().setFloorType(floorType);
        } else {
            property.getPropertyDetail().setFloorType(null);
        }
        if (roofTypeId != null && roofTypeId != -1) {
            final RoofType roofType = (RoofType) getPropPerServ().find("From RoofType where id = ?", roofTypeId);
            property.getPropertyDetail().setRoofType(roofType);
        } else {
            property.getPropertyDetail().setRoofType(null);
        }
        if (wallTypeId != null && wallTypeId != -1) {
            final WallType wallType = (WallType) getPropPerServ().find("From WallType where id = ?", wallTypeId);
            property.getPropertyDetail().setWallType(wallType);
        } else {
            property.getPropertyDetail().setWallType(null);
        }
        if (woodTypeId != null && woodTypeId != -1) {
            final WoodType woodType = (WoodType) getPropPerServ().find("From WoodType where id = ?", woodTypeId);
            property.getPropertyDetail().setWoodType(woodType);
        } else {
            property.getPropertyDetail().setWoodType(null);
        }
        if (taxExemptId != null && taxExemptId != -1) {
            final TaxExeptionReason taxExemptionReason = (TaxExeptionReason) getPropPerServ().find(
                    "From TaxExeptionReason where id = ?", taxExemptId);
            property.setTaxExemptedReason(taxExemptionReason);
            property.setIsExemptedFromTax(Boolean.TRUE);
        }

        if (areaOfPlot != null && !areaOfPlot.isEmpty()) {
            final Area area = new Area();
            area.setArea(new Float(areaOfPlot));
            property.getPropertyDetail().setSitalArea(area);
        }

        if (property.getPropertyDetail().getApartment() != null
                && property.getPropertyDetail().getApartment().getId() != null) {
            final Apartment apartment = (Apartment) getPropPerServ().find("From Apartment where id = ?",
                    property.getPropertyDetail().getApartment().getId());
            property.getPropertyDetail().setApartment(apartment);
        } else
            property.getPropertyDetail().setApartment(null);

        if (nonResPlotArea != null && !nonResPlotArea.isEmpty()) {
            final Area area = new Area();
            area.setArea(new Float(nonResPlotArea));
            property.getPropertyDetail().setNonResPlotArea(area);
        }

        property.getPropertyDetail().setFieldVerified('Y');
        property.getPropertyDetail().setProperty(property);
        final PropertyMutationMaster propMutMstr = (PropertyMutationMaster) getPropPerServ().find(
                "from PropertyMutationMaster PM where upper(PM.code) = ?", mutationCode);
        final PropertyTypeMaster propTypeMstr = (PropertyTypeMaster) getPropPerServ().find(
                "from PropertyTypeMaster PTM where PTM.id = ?", Long.valueOf(propTypeId));
        if (propUsageId != null) {
            final PropertyUsage usage = (PropertyUsage) getPropPerServ().find("from PropertyUsage pu where pu.id = ?",
                    Long.valueOf(propUsageId));
            property.getPropertyDetail().setPropertyUsage(usage);
        } else
            property.getPropertyDetail().setPropertyUsage(null);
        if (propOccId != null) {
            final PropertyOccupation occupancy = (PropertyOccupation) getPropPerServ().find(
                    "from PropertyOccupation po where po.id = ?", Long.valueOf(propOccId));
            property.getPropertyDetail().setPropertyOccupation(occupancy);
        } else
            property.getPropertyDetail().setPropertyOccupation(null);
        if (propTypeMstr.getCode().equals(OWNERSHIP_TYPE_VAC_LAND))
            property.getPropertyDetail().setPropertyType(VACANT_PROPERTY);
        else
            property.getPropertyDetail().setPropertyType(BUILT_UP_PROPERTY);

        property.getPropertyDetail().setPropertyTypeMaster(propTypeMstr);
        property.getPropertyDetail().setPropertyMutationMaster(propMutMstr);
        property.getPropertyDetail().setUpdatedTime(new Date());
        createFloors(property, mutationCode, propUsageId, propOccId);
        property.setStatus(status);
        property.setIsDefaultProperty(PROPERTY_IS_DEFAULT);
        property.setInstallment(currentInstall);
        property.setEffectiveDate(currentInstall.getFromDate());
        property.setPropertySource(propertySource);
        property.setDocNumber(docnumber);
        // TODO move this code out side this api as this dont have to be called
        // every time we create property
        if (property.getApplicationNo() == null)
            property.setApplicationNo(applicationNumberGenerator.generate());
        LOGGER.debug("Exiting from createProperty");
        return property;
    }

    /**
     * Creates floors for a property by getting list of floors from the property
     * details proxy, by removing the existing floors from property detail if
     * any
     *
     * @param property
     * @param mutationCode
     * @param propUsageId
     * @param propOccId
     */
    public void createFloors(final Property property, final String mutationCode, final String propUsageId,
            final String propOccId) {
        LOGGER.debug("Entered into createFloors");
        LOGGER.debug("createFloors: Property: " + property + ", mutationCode: " + mutationCode + ", propUsageId: "
                + propUsageId + ", propOccId: " + propOccId);
        final Area totBltUpArea = new Area();
        Float totBltUpAreaVal = new Float(0);
        if (!property.getPropertyDetail().getPropertyTypeMaster().getCode().equalsIgnoreCase(OWNERSHIP_TYPE_VAC_LAND)) {
            property.getPropertyDetail().getFloorDetails().clear();
            for (final Floor floor : property.getPropertyDetail().getFloorDetailsProxy())
                if (floor != null) {
                    totBltUpAreaVal = totBltUpAreaVal + floor.getBuiltUpArea().getArea();
                    PropertyTypeMaster unitType = null;
                    PropertyUsage usage = null;
                    PropertyOccupation occupancy = null;
                    if (floor.getUnitType() != null)
                        unitType = (PropertyTypeMaster) getPropPerServ().find(
                                "from PropertyTypeMaster utype where utype.id = ?", floor.getUnitType().getId());
                    if (floor.getPropertyUsage() != null)
                        usage = (PropertyUsage) getPropPerServ().find("from PropertyUsage pu where pu.id = ?",
                                floor.getPropertyUsage().getId());
                    if (floor.getPropertyOccupation() != null)
                        occupancy = (PropertyOccupation) getPropPerServ().find(
                                "from PropertyOccupation po where po.id = ?", floor.getPropertyOccupation().getId());

                    StructureClassification structureClass = null;

                    if (floor.getStructureClassification() != null)
                        structureClass = (StructureClassification) getPropPerServ().find(
                                "from StructureClassification sc where sc.id = ?",
                                floor.getStructureClassification().getId());

                    if (floor.getOccupancyDate() != null)
                        floor.setDepreciationMaster(propertyTaxUtil.getDepreciationByDate(floor.getOccupancyDate()));

                    LOGGER.debug("createFloors: PropertyUsage: " + usage + ", PropertyOccupation: " + occupancy
                            + ", StructureClass: " + structureClass);

                    if (unitType != null
                            && unitType.getCode().equalsIgnoreCase(PropertyTaxConstants.UNITTYPE_OPEN_PLOT))
                        floor.setFloorNo(OPEN_PLOT_UNIT_FLOORNUMBER);

                    floor.setUnitType(unitType);
                    floor.setPropertyUsage(usage);
                    floor.setPropertyOccupation(occupancy);
                    floor.setStructureClassification(structureClass);
                    floor.setPropertyDetail(property.getPropertyDetail());
                    floor.setCreatedDate(new Date());
                    floor.setModifiedDate(new Date());
                    final User user = userService.getUserById(EgovThreadLocals.getUserId());
                    floor.setCreatedBy(user);
                    floor.setModifiedBy(user);
                    property.getPropertyDetail().getFloorDetails().add(floor);
                    // setting total builtup area.
                    totBltUpArea.setArea(totBltUpAreaVal);
                    property.getPropertyDetail().setTotalBuiltupArea(totBltUpArea);

                }
            property.getPropertyDetail().setNoofFloors(property.getPropertyDetail().getFloorDetailsProxy().size());
        } else {
            property.getPropertyDetail().setNoofFloors(0);
            property.getPropertyDetail().getFloorDetails().clear();
            totBltUpArea.setArea(totBltUpAreaVal);
            property.getPropertyDetail().setTotalBuiltupArea(totBltUpArea);
        }

        LOGGER.debug("Exiting from createFloors");
    }

    /**
     * Creates property status values based on the status code
     *
     * @param basicProperty
     * @param statusCode
     * @param propCompletionDate
     * @param courtOrdNum
     * @param orderDate
     * @param judgmtDetails
     * @param parentPropId
     * @return PropertyImpl
     */
    public PropertyStatusValues createPropStatVal(final BasicProperty basicProperty, final String statusCode,
            final Date propCompletionDate, final String courtOrdNum, final Date orderDate, final String judgmtDetails,
            final String parentPropId) {
        LOGGER.debug("Entered into createPropStatVal");
        LOGGER.debug("createPropStatVal: basicProperty: " + basicProperty + ", statusCode: " + statusCode
                + ", propCompletionDate: " + propCompletionDate + ", courtOrdNum: " + courtOrdNum + ", orderDate: "
                + orderDate + ", judgmtDetails: " + judgmtDetails + ", parentPropId: " + parentPropId);
        final PropertyStatusValues propStatVal = new PropertyStatusValues();
        final PropertyStatus propertyStatus = (PropertyStatus) getPropPerServ().find(
                "from PropertyStatus where statusCode=?", statusCode);
        if (PROPERTY_MODIFY_REASON_ADD_OR_ALTER.equals(statusCode) || PROPERTY_MODIFY_REASON_AMALG.equals(statusCode)
                || PROPERTY_MODIFY_REASON_BIFURCATE.equals(statusCode) || PROP_CREATE_RSN.equals(statusCode))
            propStatVal.setIsActive("W");
        else
            propStatVal.setIsActive("Y");
        final User user = userService.getUserById(EgovThreadLocals.getUserId());
        propStatVal.setCreatedDate(new Date());
        propStatVal.setModifiedDate(new Date());
        propStatVal.setCreatedBy(user);
        propStatVal.setModifiedBy(user);
        propStatVal.setPropertyStatus(propertyStatus);
        if (orderDate != null || courtOrdNum != null && !courtOrdNum.equals("") || judgmtDetails != null
                && !judgmtDetails.equals("")) {
            propStatVal.setReferenceDate(orderDate);
            propStatVal.setReferenceNo(courtOrdNum);
            propStatVal.setRemarks(judgmtDetails);
        } else {
            propStatVal.setReferenceDate(new Date());
            propStatVal.setReferenceNo("0001");// There should be rule to create
            // order number, client has to give it
        }
        if (!statusCode.equals(PROP_CREATE_RSN) && propCompletionDate != null) {
            // persist the DateOfCompletion in case of modify property for
            // future reference
            final String propCompDateStr = dateFormatter.format(propCompletionDate);
            propStatVal.setExtraField1(propCompDateStr);
        }
        propStatVal.setBasicProperty(basicProperty);
        if (basicProperty.getPropertyMutationMaster() != null
                && basicProperty.getPropertyMutationMaster().getCode().equals(PROP_CREATE_RSN_BIFUR)) {
            final BasicProperty referenceBasicProperty = (BasicProperty) propPerServ.find(
                    "from BasicPropertyImpl bp where bp.upicNo=?", parentPropId);
            propStatVal.setReferenceBasicProperty(referenceBasicProperty);
        }
        LOGGER.debug("createPropStatVal: PropertyStatusValues: " + propStatVal);
        LOGGER.debug("Exiting from createPropStatVal");
        return propStatVal;
    }

    /**
     * Creates installment wise demands for a property
     *
     * @param property
     * @param dateOfCompletion
     * @return Property with installment wise demand set
     */
    public Property createDemand(final PropertyImpl property, final Date dateOfCompletion) {
        LOGGER.debug("Entered into createDemand");
        LOGGER.debug("createDemand: Property: " + property + ", dateOfCompletion: " + dateOfCompletion);

        instTaxMap = taxCalculator.calculatePropertyTax(property, dateOfCompletion);

        Ptdemand ptDemand;
        final Set<Ptdemand> ptDmdSet = new HashSet<Ptdemand>();
        Set<EgDemandDetails> dmdDetailSet;
        List<Installment> instList = new ArrayList<Installment>();
        instList = new ArrayList<Installment>(instTaxMap.keySet());
        LOGGER.debug("createDemand: instList: " + instList);
        currentInstall = PropertyTaxUtil.getCurrentInstallment();
        property.getPtDemandSet().clear(); // clear the existing demand set
        for (final Installment installment : instList) {
            final APTaxCalculationInfo taxCalcInfo = (APTaxCalculationInfo) instTaxMap.get(installment);
            dmdDetailSet = createAllDmdDetails(installment, instList, instTaxMap);
            final PTDemandCalculations ptDmdCalc = new PTDemandCalculations();
            ptDemand = new Ptdemand();
            ptDemand.setBaseDemand(taxCalcInfo.getTotalTaxPayable()); // shld be
                                                                      // updated
                                                                      // in
                                                                      // create-edit
                                                                      // mode
            ptDemand.setCreateDate(new Date());
            ptDemand.setEgInstallmentMaster(installment);
            ptDemand.setEgDemandDetails(dmdDetailSet); // clear the existing and
                                                       // recreate the
                                                       // EgDemandDetails
            ptDemand.setIsHistory("N");
            ptDemand.setEgptProperty(property);
            ptDmdSet.add(ptDemand);

            ptDmdCalc.setPtDemand(ptDemand);
            ptDmdCalc.setPropertyTax(taxCalcInfo.getTotalTaxPayable());
            ptDmdCalc.setTaxInfo(taxCalcInfo.getTaxCalculationInfoXML().getBytes());
            propPerServ.applyAuditing(ptDmdCalc);
            ptDemand.setDmdCalculations(ptDmdCalc);

            // In case of Property Type as (Open Plot,State Govt,Central Govt),
            // set the alv to PTDemandCalculations
            if (property.getPropertyDetail().getPropertyTypeMaster().getCode()
                    .equalsIgnoreCase(OWNERSHIP_TYPE_VAC_LAND))
                ptDmdCalc.setAlv(taxCalcInfo.getTotalNetARV());
            else if (installment.equals(currentInstall)){
            	// FloorwiseDemandCalculations should be set only for the
                // current installment for each floor.
                for (final Floor floor : property.getPropertyDetail().getFloorDetails()){
                	ptDmdCalc.addFlrwiseDmdCalculations(createFloorDmdCalc(ptDmdCalc, floor, taxCalcInfo));
                }
                ptDmdCalc.setAlv(totalAlv);
            }
        }
        property.getPtDemandSet().addAll(ptDmdSet);

        LOGGER.debug("Exiting from createDemand");
        return property;
    }

    /**
     * Called to modify Property demands when the property is modified
     *
     * @param oldProperty
     * @param newProperty
     * @param dateOfCompletion
     * @return newProperty
     */
    public Property createDemandForModify(final Property oldProperty, final Property newProperty,
            final Date dateOfCompletion) {
        LOGGER.debug("Entered into createDemandForModify");
        LOGGER.debug("createDemandForModify: oldProperty: " + oldProperty + ", newProperty: " + newProperty
                + ", dateOfCompletion: " + dateOfCompletion);

        List<Installment> instList = new ArrayList<Installment>();
        instList = new ArrayList<Installment>(instTaxMap.keySet());
        LOGGER.debug("createDemandForModify: instList: " + instList);
        Ptdemand ptDemandOld = new Ptdemand();
        Ptdemand ptDemandNew = new Ptdemand();
        final Installment currentInstall = PropertyTaxUtil.getCurrentInstallment();
        final Map<String, Ptdemand> oldPtdemandMap = getPtdemandsAsInstMap(oldProperty.getPtDemandSet());
        ptDemandOld = oldPtdemandMap.get(currentInstall.getDescription());
        final PropertyTypeMaster oldPropTypeMaster = oldProperty.getPropertyDetail().getPropertyTypeMaster();
        final PropertyTypeMaster newPropTypeMaster = newProperty.getPropertyDetail().getPropertyTypeMaster();

        if (!oldProperty.getPropertyDetail().getPropertyTypeMaster().getCode()
                .equalsIgnoreCase(newProperty.getPropertyDetail().getPropertyTypeMaster().getCode())
                || !oldProperty.getIsExemptedFromTax() ^ !newProperty.getIsExemptedFromTax())
            for (final Installment installment : instList)
                createAllDmdDetails(oldProperty, newProperty, installment, instList, instTaxMap);

        final Map<String, Ptdemand> newPtdemandMap = getPtdemandsAsInstMap(newProperty.getPtDemandSet());
        ptDemandNew = newPtdemandMap.get(currentInstall.getDescription());

        final Map<Installment, Set<EgDemandDetails>> newDemandDtlsMap = getEgDemandDetailsSetAsMap(new ArrayList(
                ptDemandNew.getEgDemandDetails()), instList);

        List<EgDemandDetails> penaltyDmdDtlsList = null;

        for (final Installment inst : instList) {

            carryForwardCollection(newProperty, inst, newDemandDtlsMap.get(inst), ptDemandOld, oldPropTypeMaster,
                    newPropTypeMaster);

            if (inst.equals(currentInstall)) {
                // carry forward the penalty from the old property to the new
                // property
                penaltyDmdDtlsList = getEgDemandDetailsListForReason(ptDemandOld.getEgDemandDetails(),
                        DEMANDRSN_CODE_PENALTY_FINES);
                if (penaltyDmdDtlsList != null && penaltyDmdDtlsList.size() > 0)
                    for (final EgDemandDetails penaltyDmdDet : penaltyDmdDtlsList)
                        ptDemandNew.getEgDemandDetails().add(
                                createDemandDetails(penaltyDmdDet.getAmount(), penaltyDmdDet.getAmtCollected(),
                                        penaltyDmdDet.getEgDemandReason(), inst));
                penaltyDmdDtlsList = getEgDemandDetailsListForReason(ptDemandOld.getEgDemandDetails(),
                        DEMANDRSN_CODE_CHQ_BOUNCE_PENALTY);
                if (penaltyDmdDtlsList != null && penaltyDmdDtlsList.size() > 0)
                    for (final EgDemandDetails penaltyDmdDet : penaltyDmdDtlsList)
                        ptDemandNew.getEgDemandDetails().add(
                                createDemandDetails(penaltyDmdDet.getAmount(), penaltyDmdDet.getAmtCollected(),
                                        penaltyDmdDet.getEgDemandReason(), inst));
            }
        }

        // sort the installment list in ascending order to start the excessColl
        // adjustment from 1st inst
        LOGGER.info("before adjustExcessCollAmt newDemandDtlsMap.size: " + newDemandDtlsMap.size());
        Collections.sort(instList);

        if (!excessCollAmtMap.isEmpty())
            adjustExcessCollectionAmount(instList, newDemandDtlsMap, ptDemandNew);

        LOGGER.debug("Exiting from createDemandForModify");
        return newProperty;
    }

    /**
     * Modifies property active demand and creates arrears demand and performs
     * the excss colletion adjustment
     *
     * @param propertyModel
     * @param oldProperty
     * @return
     */
    public Property modifyDemand(final PropertyImpl propertyModel, final PropertyImpl oldProperty) {
        Date propCompletionDate = null;
        if (!propertyModel.getPropertyDetail().getPropertyTypeMaster().getCode()
                .equalsIgnoreCase(OWNERSHIP_TYPE_VAC_LAND))
            propCompletionDate = getLowestDtOfCompFloorWise(propertyModel.getPropertyDetail().getFloorDetails());
        else
            propCompletionDate = propertyModel.getPropertyDetail().getDateOfCompletion();
        final PropertyImpl newProperty = (PropertyImpl) createDemand(propertyModel, propCompletionDate);
        Property modProperty = null;
        if (oldProperty == null)
            LOGGER.info("modifyBasicProp, Could not get the previous property. DCB for arrears will be incorrect");
        else {
            modProperty = createDemandForModify(oldProperty, newProperty, propCompletionDate);
            modProperty = createArrearsDemand(oldProperty, propCompletionDate, newProperty);
        }

        Map<Installment, Set<EgDemandDetails>> demandDetailsSetByInstallment = null;
        List<Installment> installments = null;

        final Set<EgDemandDetails> oldEgDemandDetailsSet = getOldDemandDetails(oldProperty, newProperty);
        demandDetailsSetByInstallment = getEgDemandDetailsSetByInstallment(oldEgDemandDetailsSet);
        installments = new ArrayList<Installment>(demandDetailsSetByInstallment.keySet());
        Collections.sort(installments);
        for (final Installment inst : installments) {
            final Map<String, BigDecimal> dmdRsnAmt = new LinkedHashMap<String, BigDecimal>();
            for (final String rsn : DEMAND_RSNS_LIST) {
                final EgDemandDetails newDmndDtls = getEgDemandDetailsForReason(
                        demandDetailsSetByInstallment.get(inst), rsn);
                if (newDmndDtls != null && newDmndDtls.getAmtCollected() != null)
                    // If there is collection then add to map
                    if (newDmndDtls.getAmtCollected().compareTo(BigDecimal.ZERO) > 0)
                        dmdRsnAmt.put(newDmndDtls.getEgDemandReason().getEgDemandReasonMaster().getCode(),
                                newDmndDtls.getAmtCollected());
            }
            getExcessCollAmtMap().put(inst, dmdRsnAmt);
        }
        final Ptdemand currentDemand = getCurrrentDemand(modProperty);
        demandDetailsSetByInstallment = getEgDemandDetailsSetByInstallment(currentDemand.getEgDemandDetails());
        installments = new ArrayList<Installment>(demandDetailsSetByInstallment.keySet());
        Collections.sort(installments);
        for (final Installment inst : installments) {
            final Map<String, BigDecimal> dmdRsnAmt = new LinkedHashMap<String, BigDecimal>();
            for (final String rsn : DEMAND_RSNS_LIST) {
                final EgDemandDetails newDmndDtls = getEgDemandDetailsForReason(
                        demandDetailsSetByInstallment.get(inst), rsn);
                if (newDmndDtls != null && newDmndDtls.getAmtCollected() != null) {
                    final BigDecimal extraCollAmt = newDmndDtls.getAmtCollected().subtract(newDmndDtls.getAmount());
                    // If there is extraColl then add to map
                    if (extraCollAmt.compareTo(BigDecimal.ZERO) > 0) {
                        dmdRsnAmt
                                .put(newDmndDtls.getEgDemandReason().getEgDemandReasonMaster().getCode(), extraCollAmt);
                        newDmndDtls.setAmtCollected(newDmndDtls.getAmtCollected().subtract(extraCollAmt));
                        newDmndDtls.setModifiedDate(new Date());
                    }
                }
            }
            getExcessCollAmtMap().put(inst, dmdRsnAmt);
        }

        LOGGER.info("Excess Collection - " + getExcessCollAmtMap());

        adjustExcessCollectionAmount(installments, demandDetailsSetByInstallment, currentDemand);
        return modProperty;
    }

    /**
     * Returns old demand details
     *
     * @param oldProperty
     * @param newProperty
     * @return set of old demand details
     */
    private Set<EgDemandDetails> getOldDemandDetails(final Property oldProperty, final Property newProperty) {

        final Set<EgDemandDetails> oldDemandDetails = new HashSet<EgDemandDetails>();

        for (final EgDemandDetails dd : getCurrrentDemand(oldProperty).getEgDemandDetails())
            if (dd.getEgDemandReason().getEgInstallmentMaster().getFromDate().before(newProperty.getEffectiveDate()))
                oldDemandDetails.add(dd);

        return oldDemandDetails;
    }

    /**
     * Prepares map of installment wise demand details set
     *
     * @param demandDetailsSet
     * @return Map of Installment wise demand details set
     */
    private Map<Installment, Set<EgDemandDetails>> getEgDemandDetailsSetByInstallment(
            final Set<EgDemandDetails> demandDetailsSet) {
        final Map<Installment, Set<EgDemandDetails>> newEgDemandDetailsSetByInstallment = new HashMap<Installment, Set<EgDemandDetails>>();

        for (final EgDemandDetails dd : demandDetailsSet) {

            if (dd.getAmtCollected() == null)
                dd.setAmtCollected(ZERO);

            if (newEgDemandDetailsSetByInstallment.get(dd.getEgDemandReason().getEgInstallmentMaster()) == null) {
                final Set<EgDemandDetails> ddSet = new HashSet<EgDemandDetails>();
                ddSet.add(dd);
                newEgDemandDetailsSetByInstallment.put(dd.getEgDemandReason().getEgInstallmentMaster(), ddSet);
            } else
                newEgDemandDetailsSetByInstallment.get(dd.getEgDemandReason().getEgInstallmentMaster()).add(dd);
        }

        return newEgDemandDetailsSetByInstallment;
    }

    /**
     * Returns current installment's demand of a property
     *
     * @param property
     * @return
     */
    private Ptdemand getCurrrentDemand(final Property property) {
        Ptdemand currentDemand = null;

        for (final Ptdemand ptdemand : property.getPtDemandSet())
            if (ptdemand.getEgInstallmentMaster().equals(PropertyTaxUtil.getCurrentInstallment())) {
                currentDemand = ptdemand;
                break;
            }
        return currentDemand;
    }

    /**
     * Returns formatted property occupation date
     *
     * @param dateOfCompletion
     * @return
     */
    public Date getPropOccupatedDate(final String dateOfCompletion) {
        LOGGER.debug("Entered into getPropOccupatedDate, dateOfCompletion: " + dateOfCompletion);
        Date occupationDate = null;
        try {
            if (dateOfCompletion != null && !"".equals(dateOfCompletion))
                occupationDate = dateFormatter.parse(dateOfCompletion);
        } catch (final ParseException e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.debug("Exiting from getPropOccupatedDate");
        return occupationDate;
    }

    /**
     * Creates installment wise demand details
     *
     * @param installment
     * @param instList
     * @param instTaxMap
     * @return
     */
    private Set<EgDemandDetails> createAllDmdDetails(final Installment installment, final List<Installment> instList,
            final HashMap<Installment, TaxCalculationInfo> instTaxMap) {
        LOGGER.debug("Entered into createAllDmdDeatails");
        /*
         * LOGGER.debug("createAllDmdDeatails: installment: " + installment +
         * ", instList: " + instList + ", instTaxMap: " + instTaxMap);
         */

        final Set<EgDemandDetails> dmdDetSet = new HashSet<EgDemandDetails>();

        for (final Installment inst : instList)
            if (inst.getFromDate().before(installment.getFromDate())
                    || inst.getFromDate().equals(installment.getFromDate())) {

                final TaxCalculationInfo taxCalcInfo = instTaxMap.get(inst);
                final Map<String, BigDecimal> taxMap = taxCalculator.getMiscTaxesForProp(taxCalcInfo
                        .getUnitTaxCalculationInfos());

                for (final Map.Entry<String, BigDecimal> tax : taxMap.entrySet()) {
                    final EgDemandReason egDmdRsn = propertyTaxUtil.getDemandReasonByCodeAndInstallment(tax.getKey(),
                            inst);
                    dmdDetSet.add(createDemandDetails(tax.getValue(), egDmdRsn, inst));
                }
            }

        LOGGER.debug("createAllDmdDeatails: dmdDetSet: " + dmdDetSet);
        return dmdDetSet;
    }

    /**
     * Modifies demand details of newly created property
     *
     * @param oldProperty
     * @param newProperty
     * @param installment
     * @param instList
     * @param instTaxMap
     */
    @SuppressWarnings("unchecked")
    private void createAllDmdDetails(final Property oldProperty, final Property newProperty,
            final Installment installment, final List<Installment> instList,
            final HashMap<Installment, TaxCalculationInfo> instTaxMap) {
        LOGGER.debug("Entered into createAllDmdDeatails");
        LOGGER.debug("createAllDmdDeatails: oldProperty: " + oldProperty + ", newProperty: " + newProperty
                + ",installment: " + installment + ", instList: " + instList);
        final Set<EgDemandDetails> adjustedDmdDetailsSet = new HashSet<EgDemandDetails>();
        final Module module = moduleDao.getModuleByName(PTMODULENAME);
        final Installment currentInstall = installmentDao.getInsatllmentByModuleForGivenDate(module, new Date());

        final Map<String, Ptdemand> oldPtdemandMap = getPtdemandsAsInstMap(oldProperty.getPtDemandSet());
        final Map<String, Ptdemand> newPtdemandMap = getPtdemandsAsInstMap(newProperty.getPtDemandSet());

        Ptdemand ptDemandOld = new Ptdemand();
        Ptdemand ptDemandNew = new Ptdemand();

        Set<EgDemandDetails> newEgDemandDetailsSet = null;
        Set<EgDemandDetails> oldEgDemandDetailsSet = null;

        final List<String> adjstmntReasons = new ArrayList<String>() {
            /**
             *
             */
            private static final long serialVersionUID = 860234856101419601L;

            {
                add(DEMANDRSN_CODE_GENERAL_TAX);
                add(DEMANDRSN_CODE_VACANT_TAX);
                add(DEMANDRSN_CODE_LIBRARY_CESS);
                add(DEMANDRSN_CODE_SEWERAGE_TAX);
                add(DEMANDRSN_CODE_EDUCATIONAL_CESS);
            }
        };

        final List<String> rsnsForNewResProp = new ArrayList<String>() {
            /**
             *
             */
            private static final long serialVersionUID = -1654413629447625291L;

            {
                add(DEMANDRSN_CODE_GENERAL_TAX);
                add(DEMANDRSN_CODE_VACANT_TAX);
                add(DEMANDRSN_CODE_LIBRARY_CESS);
                add(DEMANDRSN_CODE_SEWERAGE_TAX);
                add(DEMANDRSN_CODE_EDUCATIONAL_CESS);
            }
        };

        new ArrayList<String>() {
            /**
             *
             */
            private static final long serialVersionUID = -8513477823231046385L;

            {
                add(DEMANDRSN_CODE_GENERAL_TAX);
                add(DEMANDRSN_CODE_VACANT_TAX);
                add(DEMANDRSN_CODE_LIBRARY_CESS);
                add(DEMANDRSN_CODE_SEWERAGE_TAX);
                add(DEMANDRSN_CODE_EDUCATIONAL_CESS);
            }
        };

        ptDemandOld = oldPtdemandMap.get(currentInstall.getDescription());
        ptDemandNew = newPtdemandMap.get(installment.getDescription());

        LOGGER.info("instList==========" + instList);

        final Map<Installment, Set<EgDemandDetails>> oldDemandDtlsMap = getEgDemandDetailsSetAsMap(new ArrayList(
                ptDemandOld.getEgDemandDetails()), instList);
        LOGGER.info("oldDemandDtlsMap : " + oldDemandDtlsMap);

        for (final Installment inst : instList) {
            oldEgDemandDetailsSet = new HashSet<EgDemandDetails>();

            oldEgDemandDetailsSet = oldDemandDtlsMap.get(inst);

            if (inst.getFromDate().before(installment.getFromDate())
                    || inst.getFromDate().equals(installment.getFromDate())) {
                LOGGER.info("inst==========" + inst);
                final Set<EgDemandDetails> demandDtls = demandDetails.get(inst);
                if (demandDtls != null)
                    for (final EgDemandDetails dd : demandDtls) {
                        final EgDemandDetails ddClone = (EgDemandDetails) dd.clone();
                        ddClone.setEgDemand(ptDemandNew);
                        adjustedDmdDetailsSet.add(ddClone);
                    }
                else {

                    EgDemandDetails oldEgdmndDetails = null;
                    EgDemandDetails newEgDmndDetails = null;

                    newEgDemandDetailsSet = new HashSet<EgDemandDetails>();

                    // Getting EgDemandDetails for inst installment

                    for (final EgDemandDetails edd : ptDemandNew.getEgDemandDetails())
                        if (edd.getEgDemandReason().getEgInstallmentMaster().equals(inst))
                            newEgDemandDetailsSet.add((EgDemandDetails) edd.clone());

                    final PropertyTypeMaster newPropTypeMaster = newProperty.getPropertyDetail()
                            .getPropertyTypeMaster();

                    LOGGER.info("Old Demand Set:" + inst + "=" + oldEgDemandDetailsSet);
                    LOGGER.info("New Demand set:" + inst + "=" + newEgDemandDetailsSet);

                    if (!oldProperty.getIsExemptedFromTax() && !newProperty.getIsExemptedFromTax())
                        for (int i = 0; i < adjstmntReasons.size(); i++) {
                            final String oldPropRsn = adjstmntReasons.get(i);
                            String newPropRsn = null;

                            /*
                             * Gives EgDemandDetails from newEgDemandDetailsSet
                             * for demand reason oldPropRsn, if we dont have
                             * EgDemandDetails then doing collection adjustments
                             */
                            newEgDmndDetails = getEgDemandDetailsForReason(newEgDemandDetailsSet, oldPropRsn);

                            if (newEgDmndDetails == null) {
                                /*
                                 * if
                                 * (newPropTypeMaster.getCode().equalsIgnoreCase
                                 * (PROPTYPE_RESD))
                                 */
                                newPropRsn = rsnsForNewResProp.get(i);
                                /*
                                 * else if
                                 * (newPropTypeMaster.getCode().equalsIgnoreCase
                                 * (PROPTYPE_NON_RESD)) newPropRsn =
                                 * rsnsForNewNonResProp.get(i);
                                 */

                                oldEgdmndDetails = getEgDemandDetailsForReason(oldEgDemandDetailsSet, oldPropRsn);
                                newEgDmndDetails = getEgDemandDetailsForReason(newEgDemandDetailsSet, newPropRsn);

                                if (newEgDmndDetails != null && oldEgdmndDetails != null)
                                    newEgDmndDetails.setAmtCollected(newEgDmndDetails.getAmtCollected().add(
                                            oldEgdmndDetails.getAmtCollected()));
                                else
                                    continue;
                            }
                        }
                    else if (!oldProperty.getIsExemptedFromTax())
                        newEgDemandDetailsSet = adjustmentsForTaxExempted(ptDemandOld.getEgDemandDetails(),
                                newEgDemandDetailsSet, inst);

                    // Collection carry forward logic (This logic is moved out
                    // of this method, bcoz it has to be invoked in all usecases
                    // and not only when there is property type change

                    newEgDemandDetailsSet = carryForwardCollection(newProperty, inst, newEgDemandDetailsSet,
                            ptDemandOld, oldProperty.getPropertyDetail().getPropertyTypeMaster(), newPropTypeMaster);
                    LOGGER.info("Adjusted set:" + inst + ":" + newEgDemandDetailsSet);
                    adjustedDmdDetailsSet.addAll(newEgDemandDetailsSet);
                    demandDetails.put(inst, newEgDemandDetailsSet);
                }
            }
        }

        // forwards the base collection for current installment Ptdemand
        if (installment.equals(currentInstall)) {
            final Ptdemand ptdOld = oldPtdemandMap.get(currentInstall.getDescription());
            final Ptdemand ptdNew = newPtdemandMap.get(currentInstall.getDescription());
            ptdNew.setAmtCollected(ptdOld.getAmtCollected());
        }

        LOGGER.info("Exit from PropertyService.createAllDmdDeatails, Modify Adjustments for "
                + oldProperty.getBasicProperty().getUpicNo() + " And installment : " + installment + "\n\n"
                + adjustedDmdDetailsSet);
        ptDemandNew.setEgDemandDetails(adjustedDmdDetailsSet);
        LOGGER.debug("Exiting from createAllDmdDeatails");
    }

    /**
     * Carry forwards collection from the old property to the newly created
     * property
     *
     * @param newProperty
     * @param inst
     * @param newEgDemandDetailsSet
     * @param ptDmndOld
     * @param oldPropTypeMaster
     * @param newPropTypeMaster
     * @return
     */
    private Set<EgDemandDetails> carryForwardCollection(final Property newProperty, final Installment inst,
            final Set<EgDemandDetails> newEgDemandDetailsSet, final Ptdemand ptDmndOld,
            final PropertyTypeMaster oldPropTypeMaster, final PropertyTypeMaster newPropTypeMaster) {
        LOGGER.debug("Entered into carryForwardCollection");
        LOGGER.debug("carryForwardCollection: newProperty: " + newProperty + ", inst: " + inst
                + ", newEgDemandDetailsSet: " + newEgDemandDetailsSet + ", ptDmndOld: " + ptDmndOld
                + ", oldPropTypeMaster: " + oldPropTypeMaster + ", newPropTypeMaster: " + newPropTypeMaster);

        final Map<String, BigDecimal> dmdRsnAmt = new LinkedHashMap<String, BigDecimal>();

        final List<String> demandReasonsWithAdvance = new ArrayList<String>(DEMAND_RSNS_LIST);
        demandReasonsWithAdvance.add(PropertyTaxConstants.DEMANDRSN_CODE_ADVANCE);

        for (final String rsn : demandReasonsWithAdvance) {

            List<EgDemandDetails> oldEgDmndDtlsList = null;
            List<EgDemandDetails> newEgDmndDtlsList = null;

            if (newProperty.getIsExemptedFromTax())
                if (!rsn.equalsIgnoreCase(DEMANDRSN_CODE_LIBRARY_CESS)
                        && !rsn.equalsIgnoreCase(DEMANDRSN_CODE_EDUCATIONAL_CESS)
                        && !rsn.equalsIgnoreCase(DEMANDRSN_CODE_UNAUTHORIZED_PENALTY))
                    continue;

            oldEgDmndDtlsList = getEgDemandDetailsListForReason(ptDmndOld.getEgDemandDetails(), rsn);
            newEgDmndDtlsList = getEgDemandDetailsListForReason(newEgDemandDetailsSet, rsn);

            Map<Installment, EgDemandDetails> oldDemandDtlsMap = null;
            Map<Installment, EgDemandDetails> newDemandDtlsMap = null;
            EgDemandDetails oldDmndDtls = null;
            EgDemandDetails newDmndDtls = null;

            if (oldEgDmndDtlsList != null) {
                oldDemandDtlsMap = getEgDemandDetailsAsMap(oldEgDmndDtlsList);
                oldDmndDtls = oldDemandDtlsMap.get(inst);
            }
            if (newEgDmndDtlsList != null) {
                newDemandDtlsMap = getEgDemandDetailsAsMap(newEgDmndDtlsList);
                newDmndDtls = newDemandDtlsMap.get(inst);
            }

            calculateExcessCollection(dmdRsnAmt, rsn, oldDmndDtls, newDmndDtls);
        }
        excessCollAmtMap.put(inst, dmdRsnAmt);

        demandDetails.put(inst, newEgDemandDetailsSet);
        LOGGER.debug("carryForwardCollection: newEgDemandDetailsSet: " + newEgDemandDetailsSet);
        LOGGER.debug("Exiting from carryForwardCollection");
        return newEgDemandDetailsSet;
    }

    /**
     * Calculates and prepares demand reason wise excess collection amount
     *
     * @param dmdRsnAmt
     * @param rsn
     * @param oldDmndDtls
     * @param newDmndDtls
     */
    public void calculateExcessCollection(final Map<String, BigDecimal> dmdRsnAmt, final String rsn,
            final EgDemandDetails oldDmndDtls, final EgDemandDetails newDmndDtls) {
        /**
         * If old and new demand details are present then set the old collection
         * amount to the new demand details else if old demand details are not
         * present then make the new collection amount as Zero
         */
        if (newDmndDtls != null && oldDmndDtls != null) {
            newDmndDtls.setAmtCollected(newDmndDtls.getAmtCollected().add(oldDmndDtls.getAmtCollected()));
            newDmndDtls.setAmtRebate(newDmndDtls.getAmtRebate().add(oldDmndDtls.getAmtRebate()));
        } else if (newDmndDtls != null && oldDmndDtls == null) {
            newDmndDtls.setAmtCollected(ZERO);
            newDmndDtls.setAmtRebate(ZERO);
        }

        /**
         * prepares reason wise extra collection amount if any of the demand
         * details has
         */
        if (newDmndDtls != null && !rsn.equalsIgnoreCase(PropertyTaxConstants.DEMANDRSN_CODE_ADVANCE)) {
            // This part of code handles the adjustment of extra collections
            // when there is decrease in tax during property modification.

            final BigDecimal extraCollAmt = newDmndDtls.getAmtCollected().subtract(newDmndDtls.getAmount());
            // If there is extraColl then add to map
            if (extraCollAmt.compareTo(BigDecimal.ZERO) > 0) {
                dmdRsnAmt.put(rsn, extraCollAmt);
                newDmndDtls.setAmtCollected(newDmndDtls.getAmtCollected().subtract(extraCollAmt));
            }
        }

        /**
         * after modify the old demand reason is not there in new property just
         * take the entire collected amount as excess collection when a unit in
         * new property is exempted from tax 16-Oct-2014 with new requirement,
         * refer card #3427
         */
        if (oldDmndDtls != null && newDmndDtls == null)
            if (oldDmndDtls.getAmtCollected().compareTo(BigDecimal.ZERO) > 0)
                dmdRsnAmt.put(rsn, oldDmndDtls.getAmtCollected());
    }

    /**
     * Called locally to get Map of Installment/Ptdemand pair
     *
     * @param ptdemandSet
     * @return
     */
    private Map<String, Ptdemand> getPtdemandsAsInstMap(final Set<Ptdemand> ptdemandSet) {
        LOGGER.debug("Entered into getPtDemandsAsInstMap, PtDemandSet: " + ptdemandSet);
        final Map<String, Ptdemand> ptDemandMap = new TreeMap<String, Ptdemand>();
        for (final Ptdemand ptDmnd : ptdemandSet)
            ptDemandMap.put(ptDmnd.getEgInstallmentMaster().getDescription(), ptDmnd);
        LOGGER.debug("getPtDemandsAsInstMap, ptDemandMap: " + ptDemandMap);
        LOGGER.debug("Exiting from getPtDemandsAsInstMap");
        return ptDemandMap;
    }

    /**
     * Called locally to get Map of Installment/EgDemandDetail pair from list of
     * EgDemandDetails
     *
     * @param demandDetailsList
     * @return demandDetailsMap
     */
    public Map<Installment, EgDemandDetails> getEgDemandDetailsAsMap(final List<EgDemandDetails> demandDetailsList) {
        LOGGER.debug("Entered into getEgDemandDetailsAsMap, demandDetailsList: " + demandDetailsList);
        final Map<Installment, EgDemandDetails> demandDetailsMap = new HashMap<Installment, EgDemandDetails>();
        for (final EgDemandDetails dmndDtls : demandDetailsList)
            demandDetailsMap.put(dmndDtls.getEgDemandReason().getEgInstallmentMaster(), dmndDtls);
        LOGGER.debug("getEgDemandDetailsAsMap: demandDetailsMap: " + demandDetailsMap);
        LOGGER.debug("Exiting from getEgDemandDetailsAsMap");
        return demandDetailsMap;
    }

    /**
     * Called locally to get Installment/Set<EgDemandDetails> pair map
     *
     * @param demandDetailsList
     * @return
     */
    public Map<Installment, Set<EgDemandDetails>> getEgDemandDetailsSetAsMap(
            final List<EgDemandDetails> demandDetailsList, final List<Installment> instList) {
        LOGGER.debug("Entered into getEgDemandDetailsSetAsMap, demandDetailsList: " + demandDetailsList
                + ", instList: " + instList);
        final Map<Installment, Set<EgDemandDetails>> demandDetailsMap = new HashMap<Installment, Set<EgDemandDetails>>();
        Set<EgDemandDetails> ddSet = null;

        for (final Installment inst : instList) {
            ddSet = new HashSet<EgDemandDetails>();
            for (final EgDemandDetails dd : demandDetailsList)
                if (dd.getEgDemandReason().getEgInstallmentMaster().equals(inst))
                    ddSet.add(dd);
            demandDetailsMap.put(inst, ddSet);
        }
        LOGGER.debug("getEgDemandDetailsSetAsMap: demandDetailsMap: " + demandDetailsMap);
        LOGGER.debug("Exiting from getEgDemandDetailsSetAsMap");
        return demandDetailsMap;
    }

    /**
     * Called locally to get EgDemandDetails from the egDemandDetailsSet for
     * demand reason demandReason
     *
     * @param egDemandDetailsSet
     * @param demandReason
     * @return EgDemandDetails
     */
    public EgDemandDetails getEgDemandDetailsForReason(final Set<EgDemandDetails> egDemandDetailsSet,
            final String demandReason) {
        LOGGER.debug("Entered into getEgDemandDetailsForReason, egDemandDetailsSet: " + egDemandDetailsSet
                + ", demandReason: " + demandReason);
        final List<Map<String, EgDemandDetails>> egDemandDetailsList = getEgDemandDetailsAsMap(egDemandDetailsSet);
        EgDemandDetails egDemandDetails = null;
        for (final Map<String, EgDemandDetails> egDmndDtlsMap : egDemandDetailsList) {
            egDemandDetails = egDmndDtlsMap.get(demandReason);
            if (egDemandDetails != null)
                break;
        }
        LOGGER.debug("getEgDemandDetailsForReason: egDemandDetails: " + egDemandDetails);
        LOGGER.debug("Exiting from getEgDemandDetailsForReason");
        return egDemandDetails;
    }

    /**
     * Called locally to get EgDemandDetails from the egDemandDetailsSet for
     * demand reason demandReason
     *
     * @param egDemandDetailsSet
     * @param demandReason
     * @return EgDemandDetails
     */
    private List<EgDemandDetails> getEgDemandDetailsListForReason(final Set<EgDemandDetails> egDemandDetailsSet,
            final String demandReason) {
        LOGGER.debug("Entered into getEgDemandDetailsListForReason: egDemandDetailsSet: " + egDemandDetailsSet
                + ", demandReason: " + demandReason);
        final List<Map<String, EgDemandDetails>> egDemandDetailsList = getEgDemandDetailsAsMap(egDemandDetailsSet);
        final List<EgDemandDetails> demandListForReason = new ArrayList<EgDemandDetails>();
        for (final Map<String, EgDemandDetails> egDmndDtlsMap : egDemandDetailsList)
            if (egDmndDtlsMap.get(demandReason) != null)
                demandListForReason.add(egDmndDtlsMap.get(demandReason));
        LOGGER.debug("getEgDemandDetailsListForReason: demandListForReason: " + demandListForReason);
        LOGGER.debug("Exiting from getEgDemandDetailsListForReason");
        return demandListForReason;
    }

    /**
     * Called locally to get the egDemandDetailsSet as list of maps with demand
     * reason as key and EgDemandDetails as value
     *
     * @param egDemandDetailsSet
     * @param installment
     * @return
     */
    public List<Map<String, EgDemandDetails>> getEgDemandDetailsAsMap(final Set<EgDemandDetails> egDemandDetailsSet) {
        LOGGER.debug("Entered into getEgDemandDetailsAsMap, egDemandDetailsSet: " + egDemandDetailsSet);
        final List<EgDemandDetails> egDemandDetailsList = new ArrayList<EgDemandDetails>(egDemandDetailsSet);
        final List<Map<String, EgDemandDetails>> egDemandDetailsListOfMap = new ArrayList<Map<String, EgDemandDetails>>();

        for (final EgDemandDetails egDmndDtls : egDemandDetailsList) {
            final Map<String, EgDemandDetails> egDemandDetailsMap = new HashMap<String, EgDemandDetails>();
            final EgDemandReasonMaster dmndRsnMstr = egDmndDtls.getEgDemandReason().getEgDemandReasonMaster();
            if (dmndRsnMstr.getCode().equalsIgnoreCase(DEMANDRSN_CODE_GENERAL_TAX))
                egDemandDetailsMap.put(DEMANDRSN_CODE_GENERAL_TAX, egDmndDtls);
            else if (dmndRsnMstr.getCode().equalsIgnoreCase(DEMANDRSN_CODE_VACANT_TAX))
                egDemandDetailsMap.put(DEMANDRSN_CODE_VACANT_TAX, egDmndDtls);
            else if (dmndRsnMstr.getCode().equalsIgnoreCase(DEMANDRSN_CODE_EDUCATIONAL_CESS))
                egDemandDetailsMap.put(DEMANDRSN_CODE_EDUCATIONAL_CESS, egDmndDtls);
            else if (dmndRsnMstr.getCode().equalsIgnoreCase(DEMANDRSN_CODE_LIBRARY_CESS))
                egDemandDetailsMap.put(DEMANDRSN_CODE_LIBRARY_CESS, egDmndDtls);
            else if (dmndRsnMstr.getCode().equalsIgnoreCase(DEMANDRSN_CODE_UNAUTHORIZED_PENALTY))
                egDemandDetailsMap.put(DEMANDRSN_CODE_UNAUTHORIZED_PENALTY, egDmndDtls);
            else if (dmndRsnMstr.getCode().equalsIgnoreCase(DEMANDRSN_CODE_PENALTY_FINES))
                egDemandDetailsMap.put(DEMANDRSN_CODE_PENALTY_FINES, egDmndDtls);
            else if (dmndRsnMstr.getCode().equalsIgnoreCase(DEMANDRSN_CODE_CHQ_BOUNCE_PENALTY))
                egDemandDetailsMap.put(DEMANDRSN_CODE_CHQ_BOUNCE_PENALTY, egDmndDtls);
            else if (dmndRsnMstr.getCode().equalsIgnoreCase(PropertyTaxConstants.DEMANDRSN_CODE_ADVANCE))
                egDemandDetailsMap.put(PropertyTaxConstants.DEMANDRSN_CODE_ADVANCE, egDmndDtls);
            egDemandDetailsListOfMap.add(egDemandDetailsMap);
        }
        LOGGER.debug("egDemandDetailsListOfMap: " + egDemandDetailsListOfMap
                + "\n Exiting from getEgDemandDetailsAsMap");
        return egDemandDetailsListOfMap;
    }

    /**
     * Called locally to Adjust EgDemandDetails for Tax Exempted property
     *
     * @param ptDemandOld
     * @param newEgDemandDetails
     * @return newEgDemandDetails
     */
    private Set<EgDemandDetails> adjustmentsForTaxExempted(final Set<EgDemandDetails> oldEgDemandDetails,
            final Set<EgDemandDetails> newEgDemandDetails, final Installment inst) {
        LOGGER.debug("Entered into adjustmentsForTaxExempted, oldEgDemandDetails: " + oldEgDemandDetails
                + ", newEgDemandDetails: " + newEgDemandDetails + ", inst:" + inst);
        BigDecimal totalCollAdjstmntAmnt = BigDecimal.ZERO;

        for (final EgDemandDetails egDmndDtls : oldEgDemandDetails)
            if (egDmndDtls.getEgDemandReason().getEgInstallmentMaster().equals(inst)) {
                final EgDemandReasonMaster egDmndRsnMstr = egDmndDtls.getEgDemandReason().getEgDemandReasonMaster();
                if (!egDmndRsnMstr.getCode().equalsIgnoreCase(DEMANDRSN_CODE_LIBRARY_CESS)
                        && !egDmndRsnMstr.getCode().equalsIgnoreCase(DEMANDRSN_CODE_EDUCATIONAL_CESS)
                        && !egDmndRsnMstr.getCode().equalsIgnoreCase(DEMANDRSN_CODE_UNAUTHORIZED_PENALTY))
                    // totalDmndAdjstmntAmnt =
                    // totalDmndAdjstmntAmnt.add(egDmndDtls.getAmount().subtract(
                    // egDmndDtls.getAmtCollected()));
                    totalCollAdjstmntAmnt = totalCollAdjstmntAmnt.add(egDmndDtls.getAmtCollected());
            }

        final List<EgDemandDetails> newEgDmndDetails = new ArrayList<EgDemandDetails>(newEgDemandDetails);

        for (final EgDemandDetails egDmndDtls : newEgDemandDetails) {

            final EgDemandReasonMaster egDmndRsnMstr = egDmndDtls.getEgDemandReason().getEgDemandReasonMaster();

            if (egDmndRsnMstr.getCode().equalsIgnoreCase(DEMANDRSN_CODE_EDUCATIONAL_CESS))
                egDmndDtls.setAmtCollected(totalCollAdjstmntAmnt.multiply(new BigDecimal("0.50")));
            else if (egDmndRsnMstr.getCode().equalsIgnoreCase(DEMANDRSN_CODE_LIBRARY_CESS))
                egDmndDtls.setAmtCollected(totalCollAdjstmntAmnt.multiply(new BigDecimal("0.25")));
            else if (egDmndRsnMstr.getCode().equalsIgnoreCase(DEMANDRSN_CODE_UNAUTHORIZED_PENALTY))
                egDmndDtls.setAmtCollected(totalCollAdjstmntAmnt.multiply(new BigDecimal("0.25")));
        }
        LOGGER.debug("newEgDmndDetails: " + newEgDmndDetails + "\nExiting from adjustmentsForTaxExempted");
        return new HashSet<EgDemandDetails>(newEgDmndDetails);
    }

    /**
     * Creates demand details for the demand reason which being passed
     *
     * @param amount
     * @param dmdRsn
     * @param inst
     * @return Demand details
     */
    private EgDemandDetails createDemandDetails(final BigDecimal amount, final EgDemandReason dmdRsn,
            final Installment inst) {
        LOGGER.debug("Entered into createDemandDetails, amount: " + amount + ", dmdRsn: " + dmdRsn + ", inst: " + inst);
        final EgDemandDetails demandDetail = new EgDemandDetails();
        demandDetail.setAmount(amount);
        demandDetail.setAmtCollected(BigDecimal.ZERO);
        demandDetail.setAmtRebate(BigDecimal.ZERO);
        demandDetail.setEgDemandReason(dmdRsn);
        demandDetail.setCreateDate(new Date());
        demandDetail.setModifiedDate(new Date());
        LOGGER.debug("demandDetail: " + demandDetail + "\nExiting from createDemandDetails");
        return demandDetail;
    }

    /**
     * Creates demand details for the demand reason which being passed and sets
     * demand and collection
     *
     * @param amount
     * @param amountCollected
     * @param dmdRsn
     * @param inst
     * @return
     */
    public EgDemandDetails createDemandDetails(final BigDecimal amount, final BigDecimal amountCollected,
            final EgDemandReason dmdRsn, final Installment inst) {
        LOGGER.debug("Entered into createDemandDetails, amount: " + amount + "amountCollected: " + amountCollected
                + ", dmdRsn: " + dmdRsn + ", inst: " + inst);
        final EgDemandDetails demandDetail = new EgDemandDetails();
        demandDetail.setAmount(amount != null ? amount : BigDecimal.ZERO);
        demandDetail.setAmtCollected(amountCollected != null ? amountCollected : BigDecimal.ZERO);
        demandDetail.setAmtRebate(BigDecimal.ZERO);
        demandDetail.setEgDemandReason(dmdRsn);
        demandDetail.setCreateDate(new Date());
        demandDetail.setModifiedDate(new Date());
        LOGGER.debug("demandDetail: " + demandDetail + "\nExiting from createDemandDetails");
        return demandDetail;
    }

    /**
     * Creates Floor wise demand calculations
     *
     * @param ptDmdCal
     * @param floor
     * @param taxCalcInfo
     * @return FloorwiseDemandCalculations
     */
    private FloorwiseDemandCalculations createFloorDmdCalc(final PTDemandCalculations ptDmdCal, final Floor floor,
            final TaxCalculationInfo taxCalcInfo) {
        // LOGGER.debug("Entered into createFloorDmdCalc, ptDmdCal: " + ptDmdCal
        // + ", floor: " + floor + ", taxCalcInfo: " + taxCalcInfo);
        final FloorwiseDemandCalculations floorDmdCalc = new FloorwiseDemandCalculations();
        floorDmdCalc.setPTDemandCalculations(ptDmdCal);
        floorDmdCalc.setFloor(floor);

        for (final UnitTaxCalculationInfo unitTax : taxCalcInfo.getUnitTaxCalculationInfos()) {
            if (FLOOR_MAP.get(floorDmdCalc.getFloor().getFloorNo()).equals(unitTax.getFloorNumber()))
                setFloorDmdCalTax(unitTax, floorDmdCalc);
        }
        totalAlv = totalAlv.add(floorDmdCalc.getAlv());
        LOGGER.debug("floorDmdCalc: " + floorDmdCalc + "\nExiting from createFloorDmdCalc");
        return floorDmdCalc;
    }

    /**
     * Sets floor demand calculation taxes
     *
     * @param unitTax
     * @param floorDmdCalc
     */
    public void setFloorDmdCalTax(final UnitTaxCalculationInfo unitTax, final FloorwiseDemandCalculations floorDmdCalc) {
        floorDmdCalc.setAlv(unitTax.getNetARV());
        floorDmdCalc.setMrv(unitTax.getMrv());
        floorDmdCalc.setCategoryAmt(unitTax.getBaseRate());
        floorDmdCalc.setTotalTaxPayble(unitTax.getTotalTaxPayable());
        for (final MiscellaneousTax miscTax : unitTax.getMiscellaneousTaxes())
            for (final MiscellaneousTaxDetail taxDetail : miscTax.getTaxDetails())
                if (PropertyTaxConstants.DEMANDRSN_CODE_GENERAL_TAX.equals(miscTax.getTaxName()))
                    floorDmdCalc.setTax1(floorDmdCalc.getTax1().add(taxDetail.getCalculatedTaxValue()));
                else if (PropertyTaxConstants.DEMANDRSN_CODE_VACANT_TAX.equals(miscTax.getTaxName()))
                    floorDmdCalc.setTax2(floorDmdCalc.getTax2().add(taxDetail.getCalculatedTaxValue()));
                else if (PropertyTaxConstants.DEMANDRSN_CODE_LIBRARY_CESS.equals(miscTax.getTaxName()))
                    floorDmdCalc.setTax3(floorDmdCalc.getTax3().add(taxDetail.getCalculatedTaxValue()));
                else if (PropertyTaxConstants.DEMANDRSN_CODE_EDUCATIONAL_CESS.equals(miscTax.getTaxName()))
                    floorDmdCalc.setTax4(floorDmdCalc.getTax4().add(taxDetail.getCalculatedTaxValue()));
                else if (PropertyTaxConstants.DEMANDRSN_CODE_SEWERAGE_TAX.equals(miscTax.getTaxName()))
                    floorDmdCalc.setTax4(floorDmdCalc.getTax5().add(taxDetail.getCalculatedTaxValue()));
                else if (PropertyTaxConstants.DEMANDRSN_CODE_UNAUTHORIZED_PENALTY.equals(miscTax.getTaxName()))
                    floorDmdCalc.setTax4(floorDmdCalc.getTax6().add(taxDetail.getCalculatedTaxValue()));
                else if (PropertyTaxConstants.DEMANDRSN_CODE_PRIMARY_SERVICE_CHARGES.equals(miscTax.getTaxName()))
                    floorDmdCalc.setTax4(floorDmdCalc.getTax7().add(taxDetail.getCalculatedTaxValue()));
    }

    /**
     * Returns least date from the floors
     *
     * @param floorList
     * @return
     */
    public Date getLowestDtOfCompFloorWise(final List<Floor> floorList) {
        LOGGER.debug("Entered into getLowestDtOfCompFloorWise, floorList: " + floorList);
        Date completionDate = null;
        for (final Floor floor : floorList) {
            Date floorDate = null;
            if (floor != null) {
                floorDate = floor.getOccupancyDate();
                if (floorDate != null)
                    if (completionDate == null)
                        completionDate = floorDate;
                    else if (completionDate.after(floorDate))
                        completionDate = floorDate;
            }
        }
        LOGGER.debug("completionDate: " + completionDate + "\nExiting from getLowestDtOfCompFloorWise");
        return completionDate;
    }

    /**
     * Creates amalgamation property status values
     *
     * @param amalgPropIds
     * @param parentBasicProperty
     */
    public void createAmalgPropStatVal(final String[] amalgPropIds, final BasicProperty parentBasicProperty) {
        LOGGER.debug("Entered into createAmalgPropStatVal, amalgPropIds(length): "
                + (amalgPropIds != null ? amalgPropIds.length : ZERO) + ", parentBasicProperty: " + parentBasicProperty);
        final List<PropertyStatusValues> activePropStatVal = propPerServ.findAllByNamedQuery(
                QUERY_PROPSTATVALUE_BY_UPICNO_CODE_ISACTIVE, parentBasicProperty.getUpicNo(), "Y",
                PropertyTaxConstants.PROP_CREATE_RSN);
        LOGGER.debug("createAmalgPropStatVal: activePropStatVal: " + activePropStatVal);
        for (final PropertyStatusValues propstatval : activePropStatVal)
            propstatval.setIsActive("N");

        for (final String amalgId : amalgPropIds)
            if (amalgId != null && !amalgId.equals("")) {
                final BasicProperty amalgBasicProp = (BasicProperty) getPropPerServ().findByNamedQuery(
                        PropertyTaxConstants.QUERY_BASICPROPERTY_BY_UPICNO, amalgId);
                final PropertyStatusValues amalgPropStatVal = new PropertyStatusValues();
                final PropertyStatus propertyStatus = (PropertyStatus) getPropPerServ().find(
                        "from PropertyStatus where statusCode=?", PROPERTY_STATUS_MARK_DEACTIVE);
                amalgPropStatVal.setIsActive("Y");
                amalgPropStatVal.setPropertyStatus(propertyStatus);
                amalgPropStatVal.setReferenceDate(new Date());
                amalgPropStatVal.setReferenceNo("0001");
                amalgPropStatVal.setRemarks("Property Amalgamated");
                amalgBasicProp.addPropertyStatusValues(amalgPropStatVal);
                // At final approval a new PropetyStatusValues has to created
                // with status INACTIVE and set the amalgBasicProp status as
                // INACTIVE and ISACTIVE as 'N'
                amalgPropStatVal.setBasicProperty(amalgBasicProp);

                final PropertyStatusValues propertyStatusValueschild = new PropertyStatusValues();
                final PropertyStatus propertyStatuschild = (PropertyStatus) getPropPerServ().find(
                        "from PropertyStatus where statusCode=?", "CREATE");
                propertyStatusValueschild.setIsActive("Y");
                propertyStatusValueschild.setPropertyStatus(propertyStatuschild);
                propertyStatusValueschild.setReferenceDate(new Date());
                propertyStatusValueschild.setReferenceNo("0001");
                propertyStatusValueschild.setReferenceBasicProperty(amalgBasicProp);
                parentBasicProperty.addPropertyStatusValues(propertyStatusValueschild);
                propertyStatusValueschild.setBasicProperty(parentBasicProperty);
                LOGGER.debug("propertyStatusValueschild: " + propertyStatusValueschild);
            }
        LOGGER.debug("Exiting from createAmalgPropStatVal");
    }

    /**
     * Creates arrears demand for newly created property
     *
     * @param oldproperty
     * @param dateOfCompletion
     * @param property
     * @return
     */
    public Property createArrearsDemand(final Property oldproperty, final Date dateOfCompletion,
            final PropertyImpl property) {
        LOGGER.debug("Entered into createArrearsDemand, oldproperty: " + oldproperty + ", dateOfCompletion: "
                + dateOfCompletion + ", property: " + property);
        Ptdemand oldPtDmd = null;
        Ptdemand currPtDmd = null;
        Ptdemand oldCurrPtDmd = null;
        final Module module = moduleDao.getModuleByName(PTMODULENAME);
        final Installment effectiveInstall = installmentDao
                .getInsatllmentByModuleForGivenDate(module, dateOfCompletion);
        final Installment currInstall = PropertyTaxUtil.getCurrentInstallment();
        for (final Ptdemand demand : property.getPtDemandSet())
            if (demand.getIsHistory().equalsIgnoreCase("N"))
                if (demand.getEgInstallmentMaster().equals(currInstall)) {
                    currPtDmd = demand;
                    break;
                }
        for (final Ptdemand ptDmd : oldproperty.getPtDemandSet())
            if (ptDmd.getIsHistory().equalsIgnoreCase("N")) {
                if (ptDmd.getEgInstallmentMaster().getFromDate().before(effectiveInstall.getFromDate())) {
                    oldPtDmd = (Ptdemand) ptDmd.clone();
                    oldPtDmd.setEgptProperty(property);
                    property.addPtDemand(oldPtDmd);
                }
                if (ptDmd.getEgInstallmentMaster().equals(currInstall))
                    oldCurrPtDmd = ptDmd;
            }

        addArrDmdDetToCurrentDmd(oldCurrPtDmd, currPtDmd, effectiveInstall);

        LOGGER.debug("Exiting from createArrearsDemand");
        return property;
    }

    /**
     * Adds arrears demand details to the current demand
     *
     * @param ptDmd
     * @param currPtDmd
     * @param effectiveInstall
     */
    private void addArrDmdDetToCurrentDmd(final Ptdemand ptDmd, final Ptdemand currPtDmd,
            final Installment effectiveInstall) {
        LOGGER.debug("Entered into addArrDmdDetToCurrentDmd. ptDmd: " + ptDmd + ", currPtDmd: " + currPtDmd);
        for (final EgDemandDetails dmdDet : ptDmd.getEgDemandDetails())
            if (dmdDet.getEgDemandReason().getEgInstallmentMaster().getFromDate()
                    .before(effectiveInstall.getFromDate()))
                currPtDmd.addEgDemandDetails((EgDemandDetails) dmdDet.clone());
        LOGGER.debug("Exiting from addArrDmdDetToCurrentDmd");
    }

    /**
     * The purpose of this api is to initiate modify property workflow once the
     * objection workflow has ended.
     *
     * @param propertyId
     *            (Is the BasicProperty upicNo)
     * @param objectionNum
     * @param objectionDate
     * @param objWfInitiator
     *            (This is the objection workflow initiator, who will be set as
     *            the initiator of modify property initiator/owner)
     */
    /*
     * public void initiateModifyWfForObjection(Long basicPropId, String
     * objectionNum, Date objectionDate, User objWfInitiator, String docNumber,
     * String modifyRsn) {
     * LOGGER.debug("Entered into initiateModifyWfForObjection, basicPropId: " +
     * basicPropId + ", objectionNum: " + objectionNum + ", objectionDate: " +
     * objectionDate + ", objWfInitiator: " + objWfInitiator); // Retrieve
     * BasicProperty by basicPropId bcoz, upicno will be generated // during
     * final approval for create property and this // api is used to initiate
     * modify workflow before upicno is generated BasicProperty basicProperty =
     * ((BasicProperty) getPropPerServ().findByNamedQuery(
     * PropertyTaxConstants.QUERY_BASICPROPERTY_BY_BASICPROPID, basicPropId));
     * basicProperty.setAllChangesCompleted(FALSE);
     * LOGGER.debug("initiateModifyWfForObjection: basicProperty: " +
     * basicProperty); PropertyImpl oldProperty = ((PropertyImpl)
     * basicProperty.getProperty()); PropertyImpl newProperty = (PropertyImpl)
     * oldProperty.createPropertyclone();
     * LOGGER.debug("initiateModifyWfForObjection: oldProperty: " + oldProperty
     * + ", newProperty: " + newProperty); List floorProxy = new ArrayList();
     * String propUsageId = null; String propOccId = null; Date
     * propCompletionDate = getPropertyCompletionDate(basicProperty,
     * newProperty); for (Floor floor :
     * newProperty.getPropertyDetail().getFloorDetails()) { if (floor != null) {
     * floorProxy.add(floor); } }
     * newProperty.getPropertyDetail().setFloorDetails(floorProxy);
     * basicProperty.addPropertyStatusValues(createPropStatVal(basicProperty,
     * PROPERTY_MODIFY_REASON_ADD_OR_ALTER, propCompletionDate, objectionNum,
     * objectionDate, null, null)); if
     * (newProperty.getPropertyDetail().getPropertyOccupation() != null) {
     * propOccId =
     * newProperty.getPropertyDetail().getPropertyOccupation().getId(
     * ).toString(); } if (newProperty.getPropertyDetail().getPropertyUsage() !=
     * null) { propUsageId =
     * newProperty.getPropertyDetail().getPropertyUsage().getId().toString(); }
     * newProperty = createProperty(newProperty, null, modifyRsn,
     * newProperty.getPropertyDetail()
     * .getPropertyTypeMaster().getId().toString(), propUsageId, propOccId,
     * STATUS_WORKFLOW, null, null, null, null, null, null);
     * newProperty.setStatus(STATUS_WORKFLOW); // Setting the property state to
     * the objection workflow initiator Position owner =
     * eisCommonsService.getPositionByUserId(objWfInitiator.getId()); String
     * desigName = owner.getDeptDesig().getDesignation().getName(); String value
     * = WFLOW_ACTION_NAME_MODIFY + ":" + desigName + "_" +
     * WF_STATE_APPROVAL_PENDING;
     * newProperty.transition(true).start().withSenderName
     * (objWfInitiator.getName())
     * .withComments(PROPERTY_WORKFLOW_STARTED).withStateValue
     * (value).withOwner(owner) .withDateInfo(new Date());
     * newProperty.setBasicProperty(basicProperty);
     * newProperty.getPtDemandSet().clear(); createDemand(newProperty,
     * propCompletionDate); createArrearsDemand(oldProperty, propCompletionDate,
     * newProperty); basicProperty.addProperty(newProperty); basicProperty =
     * basicPropertyService.update(basicProperty);
     * LOGGER.debug("Exiting from initiateModifyWfForObjection"); }
     */
    public PropertyImpl creteNewPropertyForObjectionWorkflow(final BasicProperty basicProperty2,
            final String objectionNum, final Date objectionDate, final User objWfInitiator, final String docNumber,
            final String modifyRsn) {

        final BasicProperty basicProperty = basicProperty2;

        basicProperty.setAllChangesCompleted(FALSE);

        LOGGER.debug("initiateModifyWfForObjection: basicProperty: " + basicProperty);

        final PropertyImpl oldProperty = (PropertyImpl) basicProperty.getProperty();
        final PropertyImpl newProperty = (PropertyImpl) oldProperty.createPropertyclone();

        LOGGER.debug("initiateModifyWfForObjection: oldProperty: " + oldProperty + ", newProperty: " + newProperty);
        final List floorProxy = new ArrayList();
        final Date propCompletionDate = getPropertyCompletionDate(basicProperty, newProperty);

        for (final Floor floor : newProperty.getPropertyDetail().getFloorDetails())
            if (floor != null) {
                basicPropertyService.applyAuditing(floor);
                floor.setPropertyDetail(newProperty.getPropertyDetail());
                floorProxy.add(floor);
            }
        newProperty.getPropertyDetail().setFloorDetails(floorProxy);
        basicProperty.addPropertyStatusValues(createPropStatVal(basicProperty, PROPERTY_MODIFY_REASON_ADD_OR_ALTER,
                propCompletionDate, objectionNum, objectionDate, null, null));
        if (newProperty.getPropertyDetail().getPropertyOccupation() != null)
            newProperty.getPropertyDetail().getPropertyOccupation().getId().toString();
        if (newProperty.getPropertyDetail().getPropertyUsage() != null)
            newProperty.getPropertyDetail().getPropertyUsage().getId().toString();

        // TODO: COPYING EXISTING OWNER AS SET.CLONE OWNER COMMENTED.

        /*
         * Set<PropertyOwnerInfo> newOwnerSet = new
         * HashSet<PropertyOwnerInfo>(); for (PropertyOwnerInfo owner :
         * oldProperty.getPropertyOwnerSet()) { newOwnerSet.add(owner); }
         * newProperty.setPropertyOwnerSet(newOwnerSet);
         */

        newProperty.setStatus(STATUS_WORKFLOW);
        newProperty.setBasicProperty(basicProperty);

        newProperty.getPtDemandSet().clear();
        // createDemand(newProperty, oldProperty, propCompletionDate, false);
        // createArrearsDemand(oldProperty, propCompletionDate, newProperty);
        basicProperty.addProperty(newProperty);

        // basicProperty = basicPrpertyService.update(basicProperty);
        if (!newProperty.getPropertyDetail().getPropertyTypeMaster().getCode()
                .equalsIgnoreCase(OWNERSHIP_TYPE_VAC_LAND)) {
            // createAttributeValues(newProperty, null);
        }

        LOGGER.debug("Exiting from creteNewPropertyForObjectionWorkflow");
        return newProperty;
    }

    /**
     * Returns property completion date based on the property type
     *
     * @param basicProperty
     * @param newProperty
     * @return
     */
    private Date getPropertyCompletionDate(final BasicProperty basicProperty, final PropertyImpl newProperty) {
        LOGGER.debug("Entered into getPropertyCompletionDate - basicProperty.upicNo=" + basicProperty.getUpicNo());
        Date propCompletionDate = null;
        final String propertyTypeMasterCode = newProperty.getPropertyDetail().getPropertyTypeMaster().getCode();
        if (propertyTypeMasterCode.equalsIgnoreCase(OWNERSHIP_TYPE_VAC_LAND)) {
            for (final PropertyStatusValues propstatval : basicProperty.getPropertyStatusValuesSet())
                if (propstatval.getExtraField1() != null)
                    try {
                        propCompletionDate = dateFormatter.parse(propstatval.getExtraField1());
                    } catch (final ParseException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                else
                    propCompletionDate = basicProperty.getPropOccupationDate();
        } else {
            final List<Floor> floorList = newProperty.getPropertyDetail().getFloorDetails();
            propCompletionDate = getLowestDtOfCompFloorWise(floorList);
            if (propCompletionDate == null)
                propCompletionDate = basicProperty.getPropOccupationDate();
        }

        LOGGER.debug("propCompletionDate=" + propCompletionDate + "\nExiting from getPropertyCompletionDate");
        return propCompletionDate;
    }

    public PersistenceService getPropPerServ() {
        return propPerServ;
    }

    public void setPropPerServ(final PersistenceService propPerServ) {
        this.propPerServ = propPerServ;
    }

    public APTaxCalculator getTaxCalculator() {
        return taxCalculator;
    }

    public void setTaxCalculator(final APTaxCalculator taxCalculator) {
        this.taxCalculator = taxCalculator;
    }

    public PropertyTaxUtil getPropertyTaxUtil() {
        return propertyTaxUtil;
    }

    public void setPropertyTaxUtil(final PropertyTaxUtil propertyTaxUtil) {
        this.propertyTaxUtil = propertyTaxUtil;
    }

    public void setBasicPropertyService(final PersistenceService<BasicProperty, Long> basicPropertyService) {
        this.basicPropertyService = basicPropertyService;
    }

    /**
     * setting property status values to Basic Property
     *
     * @param basicProperty
     */
    public void setWFPropStatValActive(final BasicProperty basicProperty) {
        LOGGER.debug("Entered into setWFPropStatValActive, basicProperty: " + basicProperty);
        for (final PropertyStatusValues psv : basicProperty.getPropertyStatusValuesSet()) {
            if (PROPERTY_MODIFY_REASON_ADD_OR_ALTER.equals(psv.getPropertyStatus().getStatusCode())
                    && psv.getIsActive().equals("W")) {
                final PropertyStatusValues activePropStatVal = (PropertyStatusValues) propPerServ.findByNamedQuery(
                        QUERY_PROPSTATVALUE_BY_UPICNO_CODE_ISACTIVE, basicProperty.getUpicNo(), "Y",
                        PROPERTY_MODIFY_REASON_ADD_OR_ALTER);
                if (activePropStatVal != null)
                    activePropStatVal.setIsActive("N");
                final PropertyStatusValues wfPropStatVal = (PropertyStatusValues) propPerServ.findByNamedQuery(
                        QUERY_PROPSTATVALUE_BY_UPICNO_CODE_ISACTIVE, basicProperty.getUpicNo(), "W",
                        PROPERTY_MODIFY_REASON_ADD_OR_ALTER);
                if (wfPropStatVal != null)
                    wfPropStatVal.setIsActive("Y");
            }
            if (PROPERTY_MODIFY_REASON_AMALG.equals(psv.getPropertyStatus().getStatusCode())
                    && psv.getIsActive().equals("W")) {
                final PropertyStatusValues activePropStatVal = (PropertyStatusValues) propPerServ.findByNamedQuery(
                        QUERY_PROPSTATVALUE_BY_UPICNO_CODE_ISACTIVE, basicProperty.getUpicNo(), "Y",
                        PROPERTY_MODIFY_REASON_AMALG);
                if (activePropStatVal != null)
                    activePropStatVal.setIsActive("N");
                final PropertyStatusValues wfPropStatVal = (PropertyStatusValues) propPerServ.findByNamedQuery(
                        QUERY_PROPSTATVALUE_BY_UPICNO_CODE_ISACTIVE, basicProperty.getUpicNo(), "W",
                        PROPERTY_MODIFY_REASON_AMALG);
                if (wfPropStatVal != null)
                    wfPropStatVal.setIsActive("Y");
            }
            if (PROPERTY_MODIFY_REASON_BIFURCATE.equals(psv.getPropertyStatus().getStatusCode())
                    && psv.getIsActive().equals("W")) {
                final PropertyStatusValues activePropStatVal = (PropertyStatusValues) propPerServ.findByNamedQuery(
                        QUERY_PROPSTATVALUE_BY_UPICNO_CODE_ISACTIVE, basicProperty.getUpicNo(), "Y",
                        PROPERTY_MODIFY_REASON_BIFURCATE);
                if (activePropStatVal != null)
                    activePropStatVal.setIsActive("N");
                final PropertyStatusValues wfPropStatVal = (PropertyStatusValues) propPerServ.findByNamedQuery(
                        QUERY_PROPSTATVALUE_BY_UPICNO_CODE_ISACTIVE, basicProperty.getUpicNo(), "W",
                        PROPERTY_MODIFY_REASON_BIFURCATE);
                LOGGER.debug("setWFPropStatValActive: wfPropStatVal: " + wfPropStatVal);
                if (wfPropStatVal != null)
                    wfPropStatVal.setIsActive("Y");
            }
            if (PROP_CREATE_RSN.equals(psv.getPropertyStatus().getStatusCode()) && psv.getIsActive().equals("W"))
                psv.setIsActive("Y");

        }
        LOGGER.debug("Exitinf from setWFPropStatValActive");
    }

    /**
     * Prepares a map of installment and respective reason wise demand for each
     * installment
     *
     * @param property
     * @return Map of installment and respective reason wise demand for each
     *         installment
     */
    public Map<Installment, Map<String, BigDecimal>> populateTaxesForVoucherCreation(final Property property) {
        LOGGER.debug("Entered into populateTaxesForVoucherCreation, property: " + property);
        Map<Installment, Map<String, BigDecimal>> amounts = new HashMap<Installment, Map<String, BigDecimal>>();
        if (instTaxMap != null) {
            /*
             * for (Map.Entry<Installment, TaxCalculationInfo> instTaxRec :
             * instTaxMap.entrySet()) { Map<String, BigDecimal> taxMap =
             * taxCalculator.getMiscTaxesForProp(instTaxRec.getValue()
             * .getConsolidatedUnitTaxCalculationInfo());
             * amounts.put(instTaxRec.getKey(), taxMap); }
             */
        } else
            amounts = prepareRsnWiseDemandForOldProp(property);
        LOGGER.debug("amounts: " + amounts + "\nExiting from populateTaxesForVoucherCreation");
        return amounts;
    }

    /**
     * Prepares a map of installment and respective reason wise demand for each
     * installment
     *
     * @param property
     * @return Map of installment and respective reason wise demand for each
     *         installment
     */
    public Map<Installment, Map<String, BigDecimal>> prepareRsnWiseDemandForOldProp(final Property property) {
        LOGGER.debug("Entered into prepareRsnWiseDemandForOldProp, property: " + property);
        Installment inst = null;
        final Map<Installment, Map<String, BigDecimal>> instWiseDmd = new HashMap<Installment, Map<String, BigDecimal>>();
        for (final Ptdemand ptdemand : property.getPtDemandSet())
            if (ptdemand.getIsHistory().equals("N")) {
                inst = ptdemand.getEgInstallmentMaster();
                final Map<String, BigDecimal> rsnWiseDmd = new HashMap<String, BigDecimal>();
                for (final EgDemandDetails dmdDet : ptdemand.getEgDemandDetails())
                    if (inst.equals(dmdDet.getEgDemandReason().getEgInstallmentMaster()))
                        if (!dmdDet.getEgDemandReason().getEgDemandReasonMaster().getCode()
                                .equalsIgnoreCase(DEMANDRSN_CODE_PENALTY_FINES)
                                && !dmdDet.getEgDemandReason().getEgDemandReasonMaster().getCode()
                                        .equalsIgnoreCase(DEMANDRSN_CODE_CHQ_BOUNCE_PENALTY))
                            rsnWiseDmd.put(dmdDet.getEgDemandReason().getEgDemandReasonMaster().getCode(),
                                    dmdDet.getAmount());
                instWiseDmd.put(inst, rsnWiseDmd);
            }
        LOGGER.debug("Exiting from prepareRsnWiseDemandForOldProp");
        return instWiseDmd;
    }

    /**
     * Prepares a map of installment and respective reason wise demand for each
     * installment
     *
     * @param property
     * @return
     */
    public Map<Installment, Map<String, BigDecimal>> prepareRsnWiseDemandForPropToBeDeactivated(final Property property) {
        LOGGER.debug("Entered into prepareRsnWiseDemandForPropToBeDeactivated, property: " + property);

        final Map<Installment, Map<String, BigDecimal>> amts = prepareRsnWiseDemandForOldProp(property);
        for (final Installment inst : amts.keySet())
            for (final String dmdRsn : amts.get(inst).keySet())
                amts.get(inst).put(dmdRsn, amts.get(inst).get(dmdRsn).negate());
        LOGGER.debug("amts: " + amts + "\n Exiting from prepareRsnWiseDemandForPropToBeDeactivated");
        return amts;
    }

    /**
     * <p>
     * Adjusts the excess collection amount to Demand Details
     * </p>
     * Ex: if there is excess collection for GEN_TAX then adjustments happens
     * from beginning installment to current installment if still there is
     * excess collecion remaining then it will be adjust to the group to which
     * GEN_TAX belongs.
     *
     * @param installments
     * @param newDemandDetailsByInstallment
     */
    public void adjustExcessCollectionAmount(final List<Installment> installments,
            final Map<Installment, Set<EgDemandDetails>> newDemandDetailsByInstallment, final Ptdemand ptDemand) {
        LOGGER.info("Entered into adjustExcessCollectionAmount");
        LOGGER.info("adjustExcessCollectionAmount: installments - " + installments
                + ", newDemandDetailsByInstallment.size - " + newDemandDetailsByInstallment.size());

        /**
         * Demand reason groups to adjust the excess collection amount if a
         * demand reason is collected fully. Ex: if GEN_TAX is collected for the
         * installment fully then remaining excess collection will be adjusted
         * to the group to which GEN_TAX belongs i.e., demandReasons1[GROUP1]
         */
        final Set<String> demandReasons1 = new LinkedHashSet<String>(Arrays.asList(DEMANDRSN_CODE_GENERAL_TAX,
                DEMANDRSN_CODE_VACANT_TAX, DEMANDRSN_CODE_EDUCATIONAL_CESS, DEMANDRSN_CODE_LIBRARY_CESS,
                DEMANDRSN_CODE_UNAUTHORIZED_PENALTY));

        final Set<String> demandReasons2 = new LinkedHashSet<String>(Arrays.asList(DEMANDRSN_CODE_GENERAL_TAX,
                DEMANDRSN_CODE_VACANT_TAX, DEMANDRSN_CODE_EDUCATIONAL_CESS, DEMANDRSN_CODE_LIBRARY_CESS,
                DEMANDRSN_CODE_UNAUTHORIZED_PENALTY));

        final Installment currerntInstallment = PropertyTaxUtil.getCurrentInstallment();

        for (final Map.Entry<Installment, Map<String, BigDecimal>> excessAmountByDemandReasonForInstallment : excessCollAmtMap
                .entrySet()) {
            LOGGER.debug("adjustExcessCollectionAmount : excessAmountByDemandReasonForInstallment - "
                    + excessAmountByDemandReasonForInstallment);

            for (final String demandReason : excessAmountByDemandReasonForInstallment.getValue().keySet()) {

                adjustExcessCollection(installments, newDemandDetailsByInstallment, demandReasons1, demandReasons2,
                        excessAmountByDemandReasonForInstallment, demandReason, false, null);

                // when the demand details is absent in all the installments /
                // fully collected(in case of current installment demand
                // details) , adjusting to its group
                // and remaining to one of group for current installment
                // if (!isDemandDetailExists) {
                final Set<String> reasons = demandReasons1.contains(demandReason) ? new LinkedHashSet<String>(
                        demandReasons1) : new LinkedHashSet<String>(demandReasons2);
                reasons.remove(demandReason);
                for (final String reason : reasons) {
                    adjustExcessCollection(installments, newDemandDetailsByInstallment, demandReasons1, demandReasons2,
                            excessAmountByDemandReasonForInstallment, reason, true, demandReason);
                    if (excessAmountByDemandReasonForInstallment.getValue().get(demandReason)
                            .compareTo(BigDecimal.ZERO) == 0)
                        break;
                }
                // }

                if (excessAmountByDemandReasonForInstallment.getValue().get(demandReason).compareTo(BigDecimal.ZERO) > 0) {

                    EgDemandDetails currentDemandDetail = getEgDemandDetailsForReason(
                            newDemandDetailsByInstallment.get(currerntInstallment),
                            PropertyTaxConstants.DEMANDRSN_CODE_ADVANCE);

                    if (currentDemandDetail == null) {
                        LOGGER.info("adjustExcessCollectionAmount - Advance demand details is not present, creating.. ");

                        currentDemandDetail = propertyTaxCollection.insertAdvanceCollection(
                                PropertyTaxConstants.DEMANDRSN_CODE_ADVANCE, excessAmountByDemandReasonForInstallment
                                        .getValue().get(demandReason), currerntInstallment);
                        ptDemand.addEgDemandDetails(currentDemandDetail);
                        newDemandDetailsByInstallment.get(currerntInstallment).add(currentDemandDetail);
                        // HibernateUtil.getCurrentSession().flush();
                    } else {
                        currentDemandDetail.setAmtCollected(currentDemandDetail.getAmtCollected().add(
                                excessAmountByDemandReasonForInstallment.getValue().get(demandReason)));
                        currentDemandDetail.setModifiedDate(new Date());

                    }
                }

                excessAmountByDemandReasonForInstallment.getValue().put(demandReason, BigDecimal.ZERO);
            }
        }
        LOGGER.info("Excess collection adjustment is successfully completed..");
        LOGGER.debug("Exiting from adjustExcessCollectionAmount");
    }

    /**
     * Adjusts Excess Collection amount to installment wise demand details
     *
     * @param installments
     * @param newDemandDetailsByInstallment
     * @param demandReasons1
     * @param demandReasons2
     * @param excessAmountByDemandReasonForInstallment
     * @param demandReason
     * @param isGroupAdjustment
     * @param reasonNotExists
     * @return
     */
    private Boolean adjustExcessCollection(final List<Installment> installments,
            final Map<Installment, Set<EgDemandDetails>> newDemandDetailsByInstallment,
            final Set<String> demandReasons1, final Set<String> demandReasons2,
            final Map.Entry<Installment, Map<String, BigDecimal>> excessAmountByDemandReasonForInstallment,
            final String demandReason, final Boolean isGroupAdjustment, final String reasonNotExists) {

        LOGGER.info("Entered into adjustExcessCollection");

        Boolean isDemandDetailExists = Boolean.FALSE;
        BigDecimal balanceDemand = BigDecimal.ZERO;

        for (final Installment installment : installments) {
            final EgDemandDetails newDemandDetail = getEgDemandDetailsForReason(
                    newDemandDetailsByInstallment.get(installment), demandReason);

            if (newDemandDetail == null)
                isDemandDetailExists = Boolean.FALSE;
            else {
                isDemandDetailExists = Boolean.TRUE;
                balanceDemand = newDemandDetail.getAmount().subtract(newDemandDetail.getAmtCollected());

                if (balanceDemand.compareTo(BigDecimal.ZERO) > 0) {

                    BigDecimal excessCollection = isGroupAdjustment ? excessAmountByDemandReasonForInstallment
                            .getValue().get(reasonNotExists) : excessAmountByDemandReasonForInstallment.getValue().get(
                            demandReason);

                    if (excessCollection.compareTo(BigDecimal.ZERO) > 0) {

                        if (excessCollection.compareTo(balanceDemand) <= 0) {
                            newDemandDetail.setAmtCollected(newDemandDetail.getAmtCollected().add(excessCollection));
                            newDemandDetail.setModifiedDate(new Date());
                            excessCollection = BigDecimal.ZERO;
                        } else {
                            newDemandDetail.setAmtCollected(newDemandDetail.getAmtCollected().add(balanceDemand));
                            newDemandDetail.setModifiedDate(new Date());
                            BigDecimal remainingExcessCollection = excessCollection.subtract(balanceDemand);

                            while (remainingExcessCollection.compareTo(BigDecimal.ZERO) > 0) {

                                /**
                                 * adjust to next installments in asc order for
                                 * the reason demandReason
                                 */

                                final Set<String> oneReason = new LinkedHashSet<String>();
                                oneReason.add(demandReason);
                                remainingExcessCollection = adjustToInstallmentDemandDetails(installments,
                                        newDemandDetailsByInstallment, excessAmountByDemandReasonForInstallment,
                                        remainingExcessCollection, oneReason);

                                if (remainingExcessCollection.compareTo(BigDecimal.ZERO) == 0)
                                    excessCollection = BigDecimal.ZERO;

                                if (remainingExcessCollection.compareTo(BigDecimal.ZERO) > 0) {
                                    final Set<String> reasons = demandReasons1.contains(demandReason) ? new LinkedHashSet<String>(
                                            demandReasons1) : new LinkedHashSet<String>(demandReasons2);
                                    reasons.remove(demandReason);

                                    remainingExcessCollection = adjustToInstallmentDemandDetails(installments,
                                            newDemandDetailsByInstallment, excessAmountByDemandReasonForInstallment,
                                            remainingExcessCollection, reasons);
                                }

                                /**
                                 * There is still remainingExcessCollection
                                 * after adjusting to demandReason[Installment1]
                                 * demandReason[Installment2] . . . . . . . . .
                                 * . . . . . demandReason[CurrentInstallment]
                                 * So, adjusting the remaining excess collection
                                 * to demandReason[currentInstallment]
                                 */
                                if (remainingExcessCollection.compareTo(BigDecimal.ZERO) > 0) {
                                    EgDemandDetails currentDemandDetail = getEgDemandDetailsForReason(
                                            newDemandDetailsByInstallment.get(PropertyTaxUtil.getCurrentInstallment()),
                                            demandReason);
                                    /**
                                     * if the demand reason does not exist in
                                     * the current installment then adjusting
                                     * the remaining excess collection to its
                                     * group
                                     */
                                    if (currentDemandDetail == null) {
                                        final Set<String> reasons = demandReasons1.contains(demandReason) ? new LinkedHashSet<String>(
                                                demandReasons1) : new LinkedHashSet<String>(demandReasons2);
                                        reasons.remove(demandReason);
                                        for (final String rsn : reasons) {
                                            currentDemandDetail = getEgDemandDetailsForReason(
                                                    newDemandDetailsByInstallment.get(PropertyTaxUtil
                                                            .getCurrentInstallment()), rsn);
                                            if (currentDemandDetail != null)
                                                break;
                                        }
                                    }

                                    currentDemandDetail.setAmtCollected(currentDemandDetail.getAmtCollected().add(
                                            remainingExcessCollection));
                                    currentDemandDetail.setModifiedDate(new Date());
                                    remainingExcessCollection = BigDecimal.ZERO;
                                    excessCollection = BigDecimal.ZERO;

                                }

                                if (remainingExcessCollection.compareTo(BigDecimal.ZERO) == 0)
                                    excessCollection = BigDecimal.ZERO;
                            }
                        }

                        if (excessCollection.compareTo(BigDecimal.ZERO) == 0) {
                            final String rsn = isGroupAdjustment ? reasonNotExists : demandReason;
                            excessAmountByDemandReasonForInstallment.getValue().put(rsn, ZERO);
                        }
                    }
                }
                final String rsn = isGroupAdjustment ? reasonNotExists : demandReason;
                if (excessAmountByDemandReasonForInstallment.getValue().get(rsn).compareTo(BigDecimal.ZERO) == 0)
                    break;
            }
        }

        LOGGER.info("Exiting from adjustExcessCollection");

        return isDemandDetailExists;
    }

    /**
     * Adjusts excess amount installment wise demand details
     *
     * @param installments
     * @param newDemandDetailsByInstallment
     * @param excessAmountByDemandReasonForInstallment
     * @param remainingExcessCollection
     * @param reasons
     * @return
     */
    private BigDecimal adjustToInstallmentDemandDetails(final List<Installment> installments,
            final Map<Installment, Set<EgDemandDetails>> newDemandDetailsByInstallment,
            final Map.Entry<Installment, Map<String, BigDecimal>> excessAmountByDemandReasonForInstallment,
            BigDecimal remainingExcessCollection, final Set<String> reasons) {
        LOGGER.debug("Entered into adjustToInstallmentDemandDetails");
        LOGGER.debug("adjustToInstallmentDemandDetails : reasons=" + reasons + ", remainingExcessCollection="
                + remainingExcessCollection);
        for (final String reason : reasons)
            for (final Installment nextInstallment : installments) {
                final EgDemandDetails nextNewDemandDetail = getEgDemandDetailsForReason(
                        newDemandDetailsByInstallment.get(nextInstallment), reason);

                if (nextNewDemandDetail != null) {
                    final BigDecimal balance = nextNewDemandDetail.getAmount().subtract(
                            nextNewDemandDetail.getAmtCollected());

                    if (balance.compareTo(BigDecimal.ZERO) > 0)
                        if (remainingExcessCollection.compareTo(balance) <= 0) {
                            nextNewDemandDetail.setAmtCollected(nextNewDemandDetail.getAmtCollected().add(
                                    remainingExcessCollection));
                            nextNewDemandDetail.setModifiedDate(new Date());
                            remainingExcessCollection = BigDecimal.ZERO;
                        } else {
                            nextNewDemandDetail.setAmtCollected(nextNewDemandDetail.getAmtCollected().add(balance));
                            nextNewDemandDetail.setModifiedDate(new Date());
                            remainingExcessCollection = remainingExcessCollection.subtract(balance);
                        }

                    if (remainingExcessCollection.compareTo(BigDecimal.ZERO) == 0)
                        break;
                }
            }
        LOGGER.debug("adjustToInstallmentDemandDetails : remainingExcessCollection=" + remainingExcessCollection);
        LOGGER.debug("Exiting from adjustToInstallmentDemandDetails");
        return remainingExcessCollection;
    }

    /**
     * Initiates data entry workflow
     *
     * @param basicProperty
     * @param initiater
     */
    public void initiateDataEntryWorkflow(BasicProperty basicProperty, final User initiater) {
        LOGGER.debug("Entered into initiateDataEntryWorkflow");

        final PropertyImpl oldProperty = (PropertyImpl) basicProperty.getProperty();
        final PropertyImpl newProperty = (PropertyImpl) oldProperty.createPropertyclone();

        // Setting the property state to the objection workflow initiator
        final Position owner = eisCommonsService.getPositionByUserId(initiater.getId());
        final String desigName = propertyTaxUtil.getDesignationName(initiater.getId());
        final String value = WFLOW_ACTION_NAME_MODIFY + ":" + desigName + "_" + WF_STATE_APPROVAL_PENDING;

        newProperty.transition(true).start().withSenderName(initiater.getName())
                .withComments(PROPERTY_WORKFLOW_STARTED).withStateValue(value).withOwner(owner)
                .withDateInfo(new Date());

        final PropertyMutationMaster propMutMstr = (PropertyMutationMaster) getPropPerServ().find(
                "from PropertyMutationMaster PM where upper(PM.code) = ?", PROPERTY_MODIFY_REASON_DATA_ENTRY);
        newProperty.getPropertyDetail().setPropertyMutationMaster(propMutMstr);
        newProperty.setStatus(PropertyTaxConstants.STATUS_WORKFLOW);
        basicProperty.addProperty(newProperty);

        basicProperty.addPropertyStatusValues(createPropStatVal(basicProperty, PROPERTY_MODIFY_REASON_ADD_OR_ALTER,
                getPropertyCompletionDate(basicProperty, newProperty), null, null, null, null));

        basicProperty = basicPropertyService.update(basicProperty);
        LOGGER.debug("Exiting from initiateDataEntryWorkflow");

    }

    /**
     * Changes property details
     *
     * @param modProperty
     * @param propDetail
     * @param numOfFloors
     * @return
     */
    public PropertyImpl changePropertyDetail(final PropertyImpl modProperty, final PropertyDetail propDetail,
            final Integer numOfFloors) {

        LOGGER.debug("Entered into changePropertyDetail, Property is Vacant Land");

        final PropertyDetail propertyDetail = modProperty.getPropertyDetail();

        propDetail.setSitalArea(propertyDetail.getSitalArea());
        propDetail.setTotalBuiltupArea(propertyDetail.getTotalBuiltupArea());
        propDetail.setCommBuiltUpArea(propertyDetail.getCommBuiltUpArea());
        propDetail.setPlinthArea(propertyDetail.getPlinthArea());
        propDetail.setCommVacantLand(propertyDetail.getCommVacantLand());
        propDetail.setCurrentCapitalValue(propertyDetail.getCurrentCapitalValue());
        propDetail.setSurveyNumber(propertyDetail.getSurveyNumber());
        propDetail.setFieldVerified(propertyDetail.getFieldVerified());
        propDetail.setFieldVerificationDate(propertyDetail.getFieldVerificationDate());
        propDetail.setFloorDetails(propertyDetail.getFloorDetails());
        propDetail.setPropertyDetailsID(propertyDetail.getPropertyDetailsID());
        propDetail.setWater_Meter_Num(propertyDetail.getWater_Meter_Num());
        propDetail.setElec_Meter_Num(propertyDetail.getElec_Meter_Num());
        propDetail.setNoofFloors(numOfFloors);
        propDetail.setFieldIrregular(propertyDetail.getFieldIrregular());
        propDetail.setDateOfCompletion(propertyDetail.getDateOfCompletion());
        propDetail.setProperty(propertyDetail.getProperty());
        propDetail.setUpdatedTime(propertyDetail.getUpdatedTime());
        propDetail.setPropertyTypeMaster(propertyDetail.getPropertyTypeMaster());
        propDetail.setPropertyType(propertyDetail.getPropertyType());
        propDetail.setInstallment(propertyDetail.getInstallment());
        propDetail.setPropertyOccupation(propertyDetail.getPropertyOccupation());
        propDetail.setPropertyMutationMaster(propertyDetail.getPropertyMutationMaster());
        propDetail.setComZone(propertyDetail.getComZone());
        propDetail.setCornerPlot(propertyDetail.getCornerPlot());

        if (numOfFloors == 0)
            propDetail.setPropertyUsage(propertyDetail.getPropertyUsage());
        else
            propDetail.setPropertyUsage(null);

        propDetail.setManualAlv(propertyDetail.getManualAlv());
        propDetail.setOccupierName(propertyDetail.getOccupierName());

        modProperty.setPropertyDetail(propDetail);

        LOGGER.debug("Exiting from changePropertyDetail");
        return modProperty;
    }

    public List<DocumentType> getDocumentTypesForTransactionType(TransactionType transactionType) {
        return documentTypePersistenceService.findAllByNamedQuery(DocumentType.DOCUMENTTYPE_BY_TRANSACTION_TYPE,
                transactionType);
    }

    /**
     * Stores Documents
     *
     * @param documents
     */
    public void processAndStoreDocument(final List<Document> documents) {
        documents.forEach(document -> {
            if (!(document.getUploads().isEmpty() || document.getUploadsContentType().isEmpty())) {
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

    /**
     * Creates or Updates Application index
     *
     * @param stateAwareObject
     * @param applictionType
     */
    public void updateIndexes(final StateAware stateAwareObject, final String applictionType) {
        // PropertyImpl property= (PropertyImpl) propertyObj;

        if (applictionType != null
                && (applictionType.equalsIgnoreCase(APPLICATION_TYPE_NEW_ASSESSENT)
                        || applictionType.equalsIgnoreCase(APPLICATION_TYPE_ALTER_ASSESSENT) || applictionType
                            .equalsIgnoreCase(APPLICATION_TYPE_BIFURCATE_ASSESSENT))) {
            final PropertyImpl property = (PropertyImpl) stateAwareObject;
            final ApplicationIndex applicationIndex = applicationIndexService.findByApplicationNumber(property
                    .getApplicationNo());
            final String url = "/ptis/view/viewProperty-viewForm.action?applicationNo=" + property.getApplicationNo()
                    + "&applicationType=" + applictionType;
            if (null == applicationIndex) {
                final ApplicationIndexBuilder applicationIndexBuilder = new ApplicationIndexBuilder(PTMODULENAME,
                        property.getApplicationNo(), new Date(), applictionType, property.getBasicProperty()
                                .getFullOwnerName(), property.getState().getValue(), url, property.getBasicProperty()
                                .getAddress().toString());
                applicationIndexService.createApplicationIndex(applicationIndexBuilder.build());
            } else {
                applicationIndex.setStatus(property.getState().getValue());
                applicationIndexService.updateApplicationIndex(applicationIndex);
            }

        } else if (applictionType != null && applictionType.equalsIgnoreCase(APPLICATION_TYPE_REVISION_PETITION)) {
            final RevisionPetition property = (RevisionPetition) stateAwareObject;
            final ApplicationIndex applicationIndex = applicationIndexService.findByApplicationNumber(property
                    .getObjectionNumber());
            final String url = "/ptis/view/viewProperty-viewForm.action?applicationNo=" + property.getObjectionNumber()
                    + "&applicationType=" + applictionType;
            if (null == applicationIndex) {
                final ApplicationIndexBuilder applicationIndexBuilder = new ApplicationIndexBuilder(PTMODULENAME,
                        property.getObjectionNumber(), property.getCreatedDate() != null ? property.getCreatedDate()
                                : new Date(), applictionType, property.getBasicProperty().getFullOwnerName(), property
                                .getState().getValue(), url, property.getBasicProperty().getAddress().toString());
                applicationIndexService.createApplicationIndex(applicationIndexBuilder.build());
            } else {
                applicationIndex.setStatus(property.getState().getValue());
                applicationIndexService.updateApplicationIndex(applicationIndex);
            }

        } else if (applictionType != null && applictionType.equalsIgnoreCase(APPLICATION_TYPE_TRANSFER_OF_OWNERSHIP)) {
            final PropertyMutation property = (PropertyMutation) stateAwareObject;
            final ApplicationIndex applicationIndex = applicationIndexService.findByApplicationNumber(property
                    .getApplicationNo());
            final String url = "/ptis/view/viewProperty-viewForm.action?applicationNo=" + property.getApplicationNo()
                    + "&applicationType=" + applictionType;
            if (null == applicationIndex) {
                final ApplicationIndexBuilder applicationIndexBuilder = new ApplicationIndexBuilder(PTMODULENAME,
                        property.getApplicationNo(), property.getCreatedDate() != null ? property.getCreatedDate()
                                : new Date(), applictionType, property.getBasicProperty().getFullOwnerName(), property
                                .getState().getValue(), url, property.getBasicProperty().getAddress().toString());
                applicationIndexService.createApplicationIndex(applicationIndexBuilder.build());
            } else {
                applicationIndex.setStatus(property.getState().getValue());
                applicationIndexService.updateApplicationIndex(applicationIndex);
            }

        }

    }

    /**
     * Returns whether assessment has demand dues or not
     *
     * @param assessmentNo
     * @return
     */
    public Boolean hasDemandDues(final String assessmentNo) {
        final BasicProperty basicProperty = basicPropertyDAO.getBasicPropertyByPropertyID(assessmentNo);
        final BigDecimal currentWaterTaxDue = getWaterTaxDues(assessmentNo);
        final Map<String, BigDecimal> propertyTaxDetails = getCurrentPropertyTaxDetails(basicProperty
                .getActiveProperty());
        final BigDecimal currentPropertyTaxDue = propertyTaxDetails.get(CURR_DMD_STR).subtract(
                propertyTaxDetails.get(CURR_COLL_STR));
        final BigDecimal arrearPropertyTaxDue = propertyTaxDetails.get(ARR_DMD_STR).subtract(
                propertyTaxDetails.get(ARR_COLL_STR));
        return currentWaterTaxDue.add(currentPropertyTaxDue).add(arrearPropertyTaxDue).longValue() > 0;
    }

    /**
     * Returns Water tax due of an assessment
     *
     * @param assessmentNo
     * @return
     */
    public BigDecimal getWaterTaxDues(final String assessmentNo) {
        final String wtmsRestURL = String.format(WTMS_TAXDUE_RESTURL,
                WebUtils.extractRequestDomainURL(ServletActionContext.getRequest(), false), assessmentNo);
        final HashMap<String, Object> waterTaxInfo = simpleRestClient.getRESTResponseAsMap(wtmsRestURL);
        return waterTaxInfo.get("totalTaxDue") == null ? BigDecimal.ZERO : new BigDecimal(
                Double.valueOf((Double) waterTaxInfo.get("totalTaxDue")));
    }

    /**
     * Returns Water tax due of an assessment
     * 
     * @param assessmentNo
     * @param request
     * @return
     */
    public BigDecimal getWaterTaxDues(final String assessmentNo, HttpServletRequest request) {
        final String wtmsRestURL = String.format(WTMS_TAXDUE_RESTURL, WebUtils.extractRequestDomainURL(request, false),
                assessmentNo);
        final HashMap<String, Object> waterTaxInfo = simpleRestClient.getRESTResponseAsMap(wtmsRestURL);
        return waterTaxInfo.get("totalTaxDue") == null ? BigDecimal.ZERO : new BigDecimal(
                Double.valueOf((Double) waterTaxInfo.get("totalTaxDue")));
    }

    /**
     * Method to validate bifurcation of property either using create assessment
     * or alter assessment
     *
     * @param propertyModel
     *            model object
     * @param basicProperty
     *            basic property of the property which is being bifurcated
     * @param reason
     *            Reason for creation or Modification
     * @return
     */
    public String validationForBifurcation(final PropertyImpl propertyModel, final BasicProperty basicProperty,
            final String reason) {
        final List<PropertyStatusValues> children = propertyStatusValuesDAO
                .getPropertyStatusValuesByReferenceBasicProperty(basicProperty);
        final Boolean parentBifurcated = isPropertyBifurcated(basicProperty);
        final Boolean childrenCreated = !children.isEmpty();
        String errorMsg = null;
        /**
         * Reason For Modification is Bifurcation of Assessment
         */
        if (PROPERTY_MODIFY_REASON_BIFURCATE.equalsIgnoreCase(reason)) {
            if (parentBifurcated && !childrenCreated)
                errorMsg = "error.child.not.created";
            else
                errorMsg = validateArea(propertyModel, basicProperty.getActiveProperty(), children);
        }
        /**
         * Reason For Modification is Alteration of Assessment
         */
        else if (PROPERTY_MODIFY_REASON_ADD_OR_ALTER.equalsIgnoreCase(reason)) {
            if (!childrenCreated) {
                if (parentBifurcated)
                    errorMsg = "error.child.not.created";
            } else if (!parentBifurcated)
                errorMsg = "error.parent.not.bifurcated";
        }
        /**
         * Reason For Creation is Bifurcation of Assessment
         */
        else if (PROP_CREATE_RSN_BIFUR.equals(reason)) {
            PropertyImpl parentProperty = null;
            if (parentBifurcated)
                parentProperty = getLatestHistoryProperty(basicProperty.getUpicNo());
            else
                parentProperty = basicProperty.getActiveProperty();
            errorMsg = validateArea(propertyModel, parentProperty, children);
        }
        return errorMsg;
    }

    /**
     * Validates parent property area with the bifurcated children and current
     * property area
     *
     * @param propertyModel
     * @param parentProperty
     * @param children
     * @return
     */
    private String validateArea(final PropertyImpl propertyModel, final PropertyImpl parentProperty,
            final List<PropertyStatusValues> children) {
        final Boolean childrenCreated = !children.isEmpty();
        BigDecimal childrenArea = BigDecimal.ZERO;
        BigDecimal parentArea = BigDecimal.ZERO;
        BigDecimal area = BigDecimal.ZERO;
        if (childrenCreated)
            for (final PropertyStatusValues child : children)
                childrenArea = getPropertyArea(childrenArea, child.getBasicProperty().getProperty());
        parentArea = getPropertyArea(parentArea, parentProperty);
        area = getPropertyArea(area, propertyModel);
        return area.add(childrenArea).compareTo(parentArea) > 0 ? "error.area.greaterThanParent" : "";
    }

    /**
     * Returns the latest history property of a basic property
     *
     * @param upicNo
     * @return
     */
    public PropertyImpl getLatestHistoryProperty(final String upicNo) {
        final PropertyImpl property = (PropertyImpl) propPerServ
                .find("from PropertyImpl prop where prop.basicProperty.upicNo = ? and prop.status = 'H' order by prop.id desc",
                        upicNo);
        return property;
    }

    /**
     * Tells whether the parent is bifurcated or not
     *
     * @param basicProperty
     * @return
     */
    public Boolean isPropertyBifurcated(final BasicProperty basicProperty) {
        Boolean propBifurcated = Boolean.FALSE;
        for (final Property property : basicProperty.getPropertySet())
            if ((PROPERTY_MODIFY_REASON_BIFURCATE.equalsIgnoreCase(property.getPropertyModifyReason()) || PROP_CREATE_RSN_BIFUR
                    .equalsIgnoreCase(property.getPropertyModifyReason()))
                    && !(STATUS_WORKFLOW.equals(property.getStatus()) || STATUS_CANCELLED.equals(property.getStatus()))) {
                propBifurcated = Boolean.TRUE;
                break;
            }
        return propBifurcated;
    }

    /**
     * Converting sqr yards to sqr meters
     * 
     * @param vacantLandArea
     * @return
     */
    public BigDecimal convertYardToSquareMeters(final Float vacantLandArea) {
        Float areaInSqMts = null;
        areaInSqMts = new Float(vacantLandArea) * new Float(SQUARE_YARD_TO_SQUARE_METER_VALUE);
        return new BigDecimal(areaInSqMts).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Returns the proper area value for bifurcation calculation
     *
     * @param area
     * @param property
     * @return
     */
    public BigDecimal getPropertyArea(BigDecimal area, final Property property) {
        final PropertyDetail propertyDetail = property.getPropertyDetail();
        if (propertyDetail.isAppurtenantLandChecked() != null && propertyDetail.isAppurtenantLandChecked())
            area = area.add(BigDecimal.valueOf(propertyDetail.getExtentAppartenauntLand()));
        else if (propertyDetail.getPropertyTypeMaster().getCode().equals(OWNERSHIP_TYPE_VAC_LAND))
            area = convertYardToSquareMeters(propertyDetail.getSitalArea().getArea());
        else
            area = area.add(BigDecimal.valueOf(propertyDetail.getSitalArea().getArea()));
        return area;
    }

    /**
     * Checks whether user is an employee or not
     *
     * @param user
     * @return
     */
    public Boolean isEmployee(final User user) {
        for (final Role role : user.getRoles())
            for (final AppConfigValues appconfig : getThirdPartyUserRoles())
                if (role != null && role.getName().equals(appconfig.getValue()))
                    return false;
        return true;
    }

    /**
     * Checks whether user is an employee or not
     *
     * @param user
     * @return
     */
    public Boolean isMeesevaUser(final User user) {
        for (final Role role : user.getRoles()) {
            if (role != null && role.getName().equalsIgnoreCase(MEESEVA_OPERATOR_ROLE))
                return true;
        }
        return false;
    }

    /**
     *
     * Getting User assignment based on designation ,department and zone boundary
     * 
     * Reading Designation and Department from appconfig values and Values should be 'Senior Assistant,Junior Assistant' for designation and
     * 'Revenue,Accounts,Administration' for department
     * @param basicProperty
     * @return
     */
    public Assignment getUserPositionByZone(final BasicProperty basicProperty) {
        final String designationStr = getDesignationForThirdPartyUser();
        final String departmentStr = getDepartmentForWorkFlow();
        String[] department = departmentStr.split(",");
        String[] designation = designationStr.split(",");
        List<Assignment> assignment = new ArrayList<Assignment>();
        for (String dept : department) {
            for (String desg : designation) {
                assignment = assignmentService.findByDepartmentDesignationAndBoundary(departmentService
                        .getDepartmentByName(dept).getId(),
                        designationService.getDesignationByName(desg).getId(), basicProperty.getPropertyID()
                                .getElectionBoundary().getId());
                if (!assignment.isEmpty())
                    break;
            }
            if (!assignment.isEmpty())
                break;
        }
        return !assignment.isEmpty() ? assignment.get(0) : null;
    }

    /**
     * Returns Department for property tax workflow
     *
     * @return
     */
    public String getDepartmentForWorkFlow() {
        String department = "";
        final List<AppConfigValues> appConfigValue = appConfigValuesService.getConfigValuesByModuleAndKey(PTMODULENAME,
                PROPERTYTAX_WORKFLOWDEPARTEMENT);
        if (null != appConfigValue && !appConfigValue.isEmpty())
            department = appConfigValue.get(0).getValue();
        return department;
    }

    /**
     * Returns Designation for third party user
     *
     * @return
     */
    public String getDesignationForThirdPartyUser() {
        final List<AppConfigValues> appConfigValue = appConfigValuesService.getConfigValuesByModuleAndKey(PTMODULENAME,
                PROPERTYTAX_WORKFLOWDESIGNATION);
        return null != appConfigValue ? appConfigValue.get(0).getValue() : null;
    }

    /**
     * Returns third party user roles
     *
     * @return
     */
    public List<AppConfigValues> getThirdPartyUserRoles() {

        final List<AppConfigValues> appConfigValueList = appConfigValuesService.getConfigValuesByModuleAndKey(
                PTMODULENAME, PROPERTYTAX_ROLEFORNONEMPLOYEE);

        return !appConfigValueList.isEmpty() ? appConfigValueList : null;

    }

    /**
     * Returns the installment in which the assessment date falls
     * 
     * @param assessmentDate
     * @return
     */
    public Installment getAssessmentEffectiveInstallment(final Date assessmentDate) {
        return installmentDao.getInsatllmentByModuleForGivenDate(moduleDao.getModuleByName(PTMODULENAME),
                assessmentDate);
    }

    /**
     * @param fromDemand
     * @param toDemand
     * @return List of property having demand between fromDemand and toDemand
     */
    public List<PropertyMaterlizeView> getPropertyByDemand(final String fromDemand, final String toDemand) {
        final StringBuilder queryStr = new StringBuilder();
        queryStr.append(
                "select distinct pmv from PropertyMaterlizeView pmv where pmv.aggrCurrDmd is not null and pmv.aggrCurrDmd>=:fromDemand ")
                .append("and pmv.aggrCurrDmd<=:toDemand ");
        final Query query = propPerServ.getSession().createQuery(queryStr.toString());
        query.setBigDecimal("fromDemand", new BigDecimal(fromDemand));
        query.setBigDecimal("toDemand", new BigDecimal(toDemand));

        final List<PropertyMaterlizeView> propertyList = query.list();
        return propertyList;
    }

    /**
     * @param locationId
     * @param houseNo
     * @param ownerName
     * @return List of property matching the input params
     */
    public List<PropertyMaterlizeView> getPropertyByLocation(final Integer locationId, final String houseNo,
            final String ownerName) {
        final StringBuilder queryStr = new StringBuilder();
        queryStr.append("select distinct pmv from PropertyMaterlizeView pmv ").append(
                " where pmv.locality.id=:locationId ");
        if (houseNo != null && !houseNo.trim().isEmpty())
            queryStr.append("and pmv.houseNo like :HouseNo ");
        if (ownerName != null && !ownerName.trim().isEmpty())
            queryStr.append("and trim(pmv.ownerName) like :OwnerName");
        final Query query = propPerServ.getSession().createQuery(queryStr.toString());
        query.setLong("locationId", locationId);
        if (houseNo != null && !houseNo.trim().isEmpty())
            query.setString("HouseNo", houseNo + "%");
        if (ownerName != null && !ownerName.trim().isEmpty())
            query.setString("OwnerName", ownerName + "%");

        final List<PropertyMaterlizeView> propertyList = query.list();
        return propertyList;
    }

    /**
     * @param zoneId
     * @param wardId
     * @param ownerName
     * @param houseNum
     * @return List of property matching the input params
     */
    public List<PropertyMaterlizeView> getPropertyByBoundary(final Long zoneId, final Long wardId,
            final String ownerName, final String houseNum) {
        final StringBuilder queryStr = new StringBuilder();
        queryStr.append(
                "select distinct pmv from PropertyMaterlizeView pmv, BasicPropertyImpl bp where pmv.basicPropertyID=bp.id ")
                .append("and bp.active='Y' and pmv.zone.id=:ZoneID and pmv.ward.id=:WardID ");
        if (houseNum != null && !houseNum.trim().isEmpty())
            queryStr.append("and pmv.houseNo like :HouseNo ");
        if (ownerName != null && !ownerName.trim().isEmpty())
            queryStr.append("and trim(pmv.ownerName) like :OwnerName");
        final Query query = propPerServ.getSession().createQuery(queryStr.toString());
        query.setLong("ZoneID", zoneId);
        query.setLong("WardID", wardId);
        if (houseNum != null && !houseNum.trim().isEmpty())
            query.setString("HouseNo", houseNum + "%");
        if (ownerName != null && !ownerName.trim().isEmpty())
            query.setString("OwnerName", ownerName + "%");

        final List<PropertyMaterlizeView> propertyList = query.list();
        return propertyList;
    }

    public List<PropertyMaterlizeView> getPropertyByDoorNo(final String doorNo) {
        final StringBuilder queryStr = new StringBuilder();
        queryStr.append("select distinct pmv from PropertyMaterlizeView pmv ");
        if (StringUtils.isNotBlank(doorNo)) {
            queryStr.append("where pmv.houseNo like :doorNo ");
        }
        final Query query = propPerServ.getSession().createQuery(queryStr.toString());
        if (StringUtils.isNotBlank(doorNo)) {
            query.setString("doorNo", doorNo + "%");
        }
        final List<PropertyMaterlizeView> propertyList = query.list();
        return propertyList;
    }

    public List<PropertyMaterlizeView> getPropertyByMobileNumber(final String MobileNo) {
        final StringBuilder queryStr = new StringBuilder();
        queryStr.append("select distinct pmv from PropertyMaterlizeView pmv ");
        if (StringUtils.isNotBlank(MobileNo)) {
            queryStr.append("where pmv.mobileNumber =:MobileNo ");
        }
        final Query query = propPerServ.getSession().createQuery(queryStr.toString());
        if (StringUtils.isNotBlank(MobileNo)) {
            query.setString("MobileNo", MobileNo);
        }
        final List<PropertyMaterlizeView> propertyList = query.list();
        return propertyList;
    }

    public Map<String, BigDecimal> getCurrentPropertyTaxDetails(final Property propertyImpl) {
        return ptDemandDAO.getDemandCollMap(propertyImpl);
    }

    public Map<Installment, Map<String, BigDecimal>> getExcessCollAmtMap() {
        return excessCollAmtMap;
    }

    public void setExcessCollAmtMap(final Map<Installment, Map<String, BigDecimal>> excessCollAmtMap) {
        this.excessCollAmtMap = excessCollAmtMap;
    }

    public EisCommonsService getEisCommonsService() {
        return eisCommonsService;
    }

    public void setEisCommonsService(final EisCommonsService eisCommonsService) {
        this.eisCommonsService = eisCommonsService;
    }

    public BigDecimal getTotalAlv() {
		return totalAlv;
	}

	public void setTotalAlv(BigDecimal totalAlv) {
		this.totalAlv = totalAlv;
	}
}
