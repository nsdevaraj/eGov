/* eGov suite of products aim to improve the internal efficiency,transparency,
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
/*
 * PropertyImpl.java Created on Oct 21, 2005
 *
 * Copyright 2005 eGovernments Foundation. All rights reserved.
 * EGOVERNMENTS PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.egov.ptis.domain.entity.property;

import static org.egov.ptis.constants.PropertyTaxConstants.BUILT_UP_PROPERTY;
import static org.egov.ptis.constants.PropertyTaxConstants.VACANT_PROPERTY;
import static org.egov.ptis.constants.PropertyTaxConstants.WFLOW_ACTION_NAME_ALTER;
import static org.egov.ptis.constants.PropertyTaxConstants.WFLOW_ACTION_NAME_BIFURCATE;
import static org.egov.ptis.constants.PropertyTaxConstants.WFLOW_ACTION_STEP_CREATE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.egov.commons.Installment;
import org.egov.exceptions.InvalidPropertyException;
import org.egov.infra.admin.master.entity.Boundary;
import org.egov.infra.persistence.entity.Address;
import org.egov.infra.workflow.entity.StateAware;
import org.egov.portal.entity.Citizen;
import org.egov.ptis.domain.entity.demand.Ptdemand;

public class PropertyImpl extends StateAware implements Property {

	private static final long serialVersionUID = -5353928708732980539L;

	private static final Logger LOGGER = Logger.getLogger(PropertyImpl.class);

	private Long id;

	private Citizen citizen;

	private PropertySource propertySource;

	private Boolean vacant;

	private BasicProperty basicProperty;

	private AbstractProperty abstractProperty;

	private Address address;

	private Character isDefaultProperty;

	private Character status = 'N';

	private Set<Ptdemand> ptDemandSet = new HashSet<Ptdemand>();

	private Character isChecked = 'N';

	private String remarks;

	private Date effectiveDate;

	private PropertyDetail propertyDetail;
	private String propertyModifyReason;
	private Installment installment;
	private BigDecimal manualAlv;
	private String occupierName;
	private Boolean isExemptedFromTax = false;
	private TaxExeptionReason taxExemptedReason;
	private String docNumber;
	private Boundary areaBndry;
	private BigDecimal alv;
	private List<Document> documents = new ArrayList<>();
	private String applicationNo;

	/**
	 * @Size(min=1) is not working when we modify a migrated property, Reason is
	 *              because for the migrated property the tax xml is not there
	 *              so when we try to modify the migrated property the active
	 *              property will not be having the unitCalculationDetails
	 *
	 */
	@Valid
	private Set<UnitCalculationDetail> unitCalculationDetails = new HashSet<UnitCalculationDetail>();

	@Override
	public String getDocNumber() {
		return docNumber;
	}

	@Override
	public void setDocNumber(final String docNumber) {
		this.docNumber = docNumber;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null)
			return false;

		if (this == obj)
			return true;

		if (!(obj instanceof PropertyImpl))
			return false;

		final PropertyImpl other = (PropertyImpl) obj;

		if (getId() != null && other.getId() != null) {
			if (getId().equals(other.getId()))
				return true;
			else
				return false;

		} else if ((getPropertySource() != null || other.getPropertySource() != null)
				&& (getBasicProperty() != null || other.getBasicProperty() != null))
			if (getPropertySource().equals(other.getPropertySource())
					&& getBasicProperty().equals(other.getBasicProperty())
					&& getInstallment().equals(other.getInstallment()) && getStatus().equals(other.getStatus()))
				return true;
			else
				return false;
		return false;

	}

	@Override
	public int hashCode() {
		int hashCode = 0;
		if (getId() != null)
			hashCode = hashCode + getId().hashCode();
		if (getPropertySource() != null && getBasicProperty() != null)
			hashCode = hashCode + getPropertySource().hashCode() + getBasicProperty().hashCode()
					+ getInstallment().hashCode() + getStatus().hashCode();

		return hashCode;
	}

	/*
	 * @Override public List<ValidationError> validate() { return new
	 * ArrayList<ValidationError>(); }
	 */
	@Override
	public boolean validateProperty() throws InvalidPropertyException {

		if (getBasicProperty() == null)
			throw new InvalidPropertyException("PropertyImpl.validate : BasicProperty is not Set, Please Check !!");

		if (getCreatedBy() == null)
			throw new InvalidPropertyException("PropertyImpl.validate : Created By is not Set, Please Check !!");
		if (getPropertySource() == null)
			throw new InvalidPropertyException("PropertyImpl.validate : PropertySource is not set, Please Check !!");
		else if (getPropertySource().validate() == false)
			throw new InvalidPropertyException(
					"PropertyImpl.validate : PropertySource validate() failed, Please Check !!");

		return true;
	}

	@Override
	public Character getStatus() {
		return status;
	}

	@Override
	public void setStatus(final Character status) {
		this.status = status;
	}

	@Override
	public Date getEffectiveDate() {
		return effectiveDate;
	}

	@Override
	public void setEffectiveDate(final Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	@Override
	public Character getIsDefaultProperty() {
		return isDefaultProperty;
	}

	@Override
	public void setIsDefaultProperty(final Character isDefaultProperty) {
		this.isDefaultProperty = isDefaultProperty;
	}

	public AbstractProperty getAbstractProperty() {
		return abstractProperty;
	}

	public void setAbstractProperty(final AbstractProperty abstractProperty) {
		this.abstractProperty = abstractProperty;
	}

	@Override
	public Address getPropertyAddress() {
		return address;
	}

	@Override
	public void setPropertyAddress(final Address address) {
		this.address = address;
	}

	@Override
	public Boolean isVacant() {
		return vacant;
	}

	@Override
	public void setVacant(final Boolean vacant) {
		this.vacant = vacant;
	}

	public Citizen getCitizen() {
		return citizen;
	}

	public void setCitizen(final Citizen citizen) {
		this.citizen = citizen;
	}

	@Override
	public void addPtDemand(final Ptdemand ptDmd) {
		getPtDemandSet().add(ptDmd);
	}

	@Override
	public void removePtDemand(final Ptdemand ptDmd) {
		getPtDemandSet().remove(ptDmd);
	}

	@Override
	public Installment getInstallment() {
		return installment;
	}

	@Override
	public void setInstallment(final Installment installment) {
		this.installment = installment;
	}

	public Boolean getVacant() {
		return vacant;
	}

	@Override
	public PropertySource getPropertySource() {
		return propertySource;
	}

	@Override
	public void setPropertySource(final PropertySource propertySource) {
		this.propertySource = propertySource;
	}

	@Override
	public BasicProperty getBasicProperty() {
		return basicProperty;
	}

	@Override
	public void setBasicProperty(final BasicProperty basicProperty) {
		this.basicProperty = basicProperty;
	}

	@Override
	public Set<Ptdemand> getPtDemandSet() {
		return ptDemandSet;
	}

	@Override
	public void setPtDemandSet(final Set<Ptdemand> ptDemandSet) {
		this.ptDemandSet = ptDemandSet;
	}

	@Override
	public PropertyDetail getPropertyDetail() {
		return propertyDetail;
	}

	@Override
	public void setPropertyDetail(final PropertyDetail propertyDetail) {
		this.propertyDetail = propertyDetail;
	}

	@Override
	public Character getIsChecked() {
		return isChecked;
	}

	@Override
	public void setIsChecked(final Character isChecked) {
		this.isChecked = isChecked;
	}

	@Override
	public String getRemarks() {
		return remarks;
	}

	@Override
	public void setRemarks(final String remarks) {
		this.remarks = remarks;
	}

	@Override
	public String getPropertyModifyReason() {
		return propertyModifyReason;
	}

	@Override
	public void setPropertyModifyReason(final String propertyModifyReason) {
		this.propertyModifyReason = propertyModifyReason;
	}

	@Override
	public Boolean getIsExemptedFromTax() {
		return isExemptedFromTax;
	}

	@Override
	public void setIsExemptedFromTax(final Boolean isExemptedFromTax) {
		this.isExemptedFromTax = isExemptedFromTax;
	}

	@Override
	public TaxExeptionReason getTaxExemptedReason() {
		return taxExemptedReason;
	}

	@Override
	public void setTaxExemptedReason(TaxExeptionReason taxExemptedReason) {
		this.taxExemptedReason = taxExemptedReason;
	}

	/*
	 * This method creates a clone of Property
	 */
	@Override
	public Property createPropertyclone() {
		LOGGER.debug("Inside PropertyImpl clone method  " + toString());
		final PropertyImpl newProp = new PropertyImpl();
		newProp.setBasicProperty(getBasicProperty());
		newProp.setId(null);
		newProp.setApplicationNo(null);
		newProp.setEffectiveDate(getEffectiveDate());
		newProp.setInstallment(getInstallment());
		newProp.setIsChecked(getIsChecked());
		newProp.setIsDefaultProperty(getIsDefaultProperty());
		newProp.setStatus(getStatus());
		newProp.setPropertyDetail(clonePropertyDetail(newProp));
		newProp.setPropertyModifyReason(getPropertyModifyReason());
		newProp.setPropertySource(getPropertySource());
		newProp.setPtDemandSet(cloneDemand());
		newProp.setRemarks(getRemarks());
		newProp.setVacant(getVacant());
		newProp.setIsExemptedFromTax(getIsExemptedFromTax());
		newProp.setTaxExemptedReason(getTaxExemptedReason());
		newProp.setDocNumber(getDocNumber());
		newProp.setState(getState());
		newProp.setCreatedDate(new Date());
		newProp.setLastModifiedDate(new Date());
		newProp.addAllUnitCalculationDetails(cloneUnitCalculationDetails());
		return newProp;
	}

	@Override
	public String toString() {
		StringBuilder sbf = new StringBuilder();
		sbf.append("Id: ").append(getId()).append("|BasicProperty: ");
		sbf = getBasicProperty() != null ? sbf.append(getBasicProperty().getUpicNo()) : sbf.append("");
		sbf.append("|IsDefaultProperty:").append(getIsDefaultProperty()).append("|Status:").append(getStatus())
				.append("|PropertySource: ").append(null != getPropertySource() ? getPropertySource().getName() : null)
				.append("|Installment: ").append(getInstallment());

		return sbf.toString();
	}

	/*
	 * This method returns Demand details as a Set
	 */
	private Set<Ptdemand> cloneDemand() {
		final Set<Ptdemand> newdemandSet = new HashSet<Ptdemand>();
		for (final Ptdemand demand : getPtDemandSet())
			newdemandSet.add((Ptdemand) demand.clone());
		return newdemandSet;
	}

	/*
	 * This method returns Property details of a property
	 */
	private PropertyDetail clonePropertyDetail(final Property newProperty) {
		PropertyDetail propDetails = null;
		if (getPropertyDetail().getPropertyType().toString().equals(BUILT_UP_PROPERTY)) {
			final BuiltUpProperty bup = (BuiltUpProperty) getPropertyDetail();
			propDetails = new BuiltUpProperty(getPropertyDetail().getSitalArea(), getPropertyDetail()
					.getTotalBuiltupArea(), getPropertyDetail().getCommBuiltUpArea(), getPropertyDetail()
					.getPlinthArea(), getPropertyDetail().getCommVacantLand(), getPropertyDetail().getNonResPlotArea(),
					bup.isIrregular(), getPropertyDetail().getSurveyNumber(), getPropertyDetail().getFieldVerified(),
					getPropertyDetail().getFieldVerificationDate(), cloneFlrDtls(), null, getPropertyDetail()
							.getWater_Meter_Num(), getPropertyDetail().getElec_Meter_Num(), getPropertyDetail()
							.getNoofFloors(), getPropertyDetail().getFieldIrregular(), newProperty,
					getPropertyDetail().getDateOfCompletion(), getPropertyDetail().getPropertyUsage(),
					getPropertyDetail().getUpdatedTime(), bup.getCreationReason(), getPropertyDetail()
							.getPropertyTypeMaster(), getPropertyDetail().getPropertyType(), getPropertyDetail()
							.getPropertyMutationMaster(), getPropertyDetail().getComZone(), getPropertyDetail()
							.getCornerPlot(), getPropertyDetail().getPropertyOccupation(), getPropertyDetail()
							.getExtentSite(), getPropertyDetail().getExtentAppartenauntLand(), getPropertyDetail()
							.getFloorType(), getPropertyDetail().getRoofType(), getPropertyDetail().getWallType(),
					getPropertyDetail().getWoodType(), getPropertyDetail().isLift(), getPropertyDetail().isToilets(),
					getPropertyDetail().isWaterTap(), getPropertyDetail().isStructure(), getPropertyDetail()
							.isElectricity(), getPropertyDetail().isAttachedBathRoom(), getPropertyDetail()
							.isWaterHarvesting(), getPropertyDetail().isCable(), getPropertyDetail().getSiteOwner(),
					getPropertyDetail().getApartment(), getPropertyDetail().getPattaNumber(), getPropertyDetail()
							.getCurrentCapitalValue(), getPropertyDetail().getMarketValue(), getPropertyDetail()
							.getCategoryType(), getPropertyDetail().getOccupancyCertificationNo(), getPropertyDetail()
							.getBuildingPermissionNo(), getPropertyDetail().getBuildingPermissionDate(),
					getPropertyDetail().getDeviationPercentage(), getPropertyDetail().isAppurtenantLandChecked(),
					getPropertyDetail().isBuildingPlanDetailsChecked(), getPropertyDetail().isCorrAddressDiff());

		} else if (getPropertyDetail().getPropertyType().toString().equals(VACANT_PROPERTY)) {
			final VacantProperty vcp = (VacantProperty) getPropertyDetail();
			propDetails = new VacantProperty(getPropertyDetail().getSitalArea(), getPropertyDetail()
					.getTotalBuiltupArea(), getPropertyDetail().getCommBuiltUpArea(), getPropertyDetail()
					.getPlinthArea(), getPropertyDetail().getCommVacantLand(), getPropertyDetail().getNonResPlotArea(),
					vcp.getIrregular(), getPropertyDetail().getSurveyNumber(), getPropertyDetail().getFieldVerified(),
					getPropertyDetail().getFieldVerificationDate(), cloneFlrDtls(), null, getPropertyDetail()
							.getWater_Meter_Num(), getPropertyDetail().getElec_Meter_Num(), getPropertyDetail()
							.getNoofFloors(), getPropertyDetail().getFieldIrregular(), getPropertyDetail()
							.getDateOfCompletion(), newProperty, getPropertyDetail().getUpdatedTime(),
					getPropertyDetail().getPropertyUsage(), vcp.getCreationReason(), getPropertyDetail()
							.getPropertyTypeMaster(), getPropertyDetail().getPropertyType(), getPropertyDetail()
							.getInstallment(), getPropertyDetail().getPropertyOccupation(), getPropertyDetail()
							.getPropertyMutationMaster(), getPropertyDetail().getComZone(), getPropertyDetail()
							.getCornerPlot(), getPropertyDetail().getExtentSite(), getPropertyDetail()
							.getExtentAppartenauntLand(), getPropertyDetail().getFloorType(), getPropertyDetail()
							.getRoofType(), getPropertyDetail().getWallType(), getPropertyDetail().getWoodType(),
					getPropertyDetail().isLift(), getPropertyDetail().isToilets(), getPropertyDetail().isWaterTap(),
					getPropertyDetail().isStructure(), getPropertyDetail().isElectricity(), getPropertyDetail()
							.isAttachedBathRoom(), getPropertyDetail().isWaterHarvesting(), getPropertyDetail()
							.isCable(), getPropertyDetail().getSiteOwner(), getPropertyDetail().getPattaNumber(),
					getPropertyDetail().getCurrentCapitalValue(), getPropertyDetail().getMarketValue(),
					getPropertyDetail().getCategoryType(), getPropertyDetail().getOccupancyCertificationNo(),
					getPropertyDetail().getBuildingPermissionNo(), getPropertyDetail().getBuildingPermissionDate(),
					getPropertyDetail().getDeviationPercentage(), getPropertyDetail().isAppurtenantLandChecked(),
					getPropertyDetail().isBuildingPlanDetailsChecked(), getPropertyDetail().isCorrAddressDiff());
		}
		return propDetails;
	}

	/*
	 * This method returns Floor details as a List
	 */
	private List<Floor> cloneFlrDtls() {
		Floor floor = null;
		final List<Floor> flrDtlsSet = new ArrayList<Floor>();
		for (final Floor flr : getPropertyDetail().getFloorDetails()) {
			floor = new Floor(flr.getConstructionTypeSet(), flr.getStructureClassification(), flr.getPropertyUsage(),
					flr.getPropertyOccupation(), flr.getFloorNo(), flr.getDepreciationMaster(), flr.getBuiltUpArea(),
					flr.getFloorArea(), flr.getWaterMeter(), flr.getElectricMeter(), null, null, flr.getRentPerMonth(),
					flr.getManualAlv(), flr.getUnitType(), flr.getUnitTypeCategory(), flr.getWaterRate(), flr.getAlv(),
					flr.getOccupancyDate(), flr.getOccupantName(), flr.getDrainage(), flr.getNoOfSeats(), flr.getFloorDmdCalc());
			flrDtlsSet.add(floor);
		}
		return flrDtlsSet;
	}

	/**
	 * Returns the clone of UnitCalculationDetail
	 *
	 * @return set of UnitCalculaitonDetail
	 */
	private Set<UnitCalculationDetail> cloneUnitCalculationDetails() {
		final Set<UnitCalculationDetail> unitCalculationDetailClones = new HashSet<UnitCalculationDetail>();

		for (final UnitCalculationDetail unitCalcDetail : getUnitCalculationDetails())
			unitCalculationDetailClones.add(new UnitCalculationDetail(unitCalcDetail));

		return unitCalculationDetailClones;
	}

        @Override
        public String getStateDetails() {
            StringBuffer stateDetails = new StringBuffer("");
            final String upicNo = getBasicProperty().getUpicNo() != null && !getBasicProperty().getUpicNo().isEmpty() ? getBasicProperty()
                    .getUpicNo()
                    : "";
            final String applicationNo = getApplicationNo() != null && !getApplicationNo().isEmpty() ? getApplicationNo()
                    : "";
            stateDetails.append(upicNo.isEmpty() ? applicationNo : upicNo).append(", ")
                    .append(getBasicProperty().getProperty().getPropertyDetail().getCategoryType()).append(", ")
                    .append(getBasicProperty().getPropertyID().getLocality().getName()).append(", ")
                    .append(getBasicProperty().getFullOwnerName());
            return stateDetails.toString();
        }

	@Override
	public BigDecimal getManualAlv() {
		return manualAlv;
	}

	@Override
	public void setManualAlv(final BigDecimal manualAlv) {
		this.manualAlv = manualAlv;
	}

	@Override
	public String getOccupierName() {
		return occupierName;
	}

	@Override
	public void setOccupierName(final String occupierName) {
		this.occupierName = occupierName;
	}

	@Override
	public Boundary getAreaBndry() {
		return areaBndry;
	}

	@Override
	public void setAreaBndry(final Boundary areaBndry) {
		this.areaBndry = areaBndry;
	}

	@Override
	public BigDecimal getAlv() {
		return alv;
	}

	@Override
	public void setAlv(final BigDecimal alv) {
		this.alv = alv;
	}

	@Override
	public Set<UnitCalculationDetail> getUnitCalculationDetails() {
		return unitCalculationDetails;
	}

	@Override
	public void setUnitCalculationDetails(final Set<UnitCalculationDetail> unitCalculationDetails) {
		this.unitCalculationDetails = unitCalculationDetails;
	}

	@Override
	public void addUnitCalculationDetails(final UnitCalculationDetail unitCalculationDetail) {
		unitCalculationDetail.setProperty(this);
		getUnitCalculationDetails().add(unitCalculationDetail);
	}

	@Override
	public void addAllUnitCalculationDetails(final Set<UnitCalculationDetail> unitCalculationDetailsSet) {
		for (final UnitCalculationDetail unitCalcDetail : unitCalculationDetailsSet)
			addUnitCalculationDetails(unitCalcDetail);
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(final Long id) {
		this.id = id;
	}

	@Override
	public String myLinkId() {
		String url = "";
		if (getState() != null
				&& getState().getValue() != null
				&& (getState().getValue().startsWith(WFLOW_ACTION_NAME_ALTER) || getState().getValue().startsWith(
						WFLOW_ACTION_NAME_BIFURCATE)))
			url = "/ptis/modify/modifyProperty-view.action?modelId=" + getId();
		else if (getState() != null && getState().getValue() != null
				&& getState().getValue().startsWith(WFLOW_ACTION_STEP_CREATE))
			url = "/ptis/create/createProperty-view.action" + "?modelId=" + getId();
		return url;
	}

	@Override
	public List<Document> getDocuments() {
		return documents;
	}

	@Override
	public void setDocuments(final List<Document> documents) {
		this.documents = documents;
	}

	@Override
	public String getApplicationNo() {
		return applicationNo;
	}

	@Override
	public void setApplicationNo(final String applicationNo) {
		this.applicationNo = applicationNo;
	}

}
