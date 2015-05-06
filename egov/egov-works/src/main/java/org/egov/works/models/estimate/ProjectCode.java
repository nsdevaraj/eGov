/**
 * eGov suite of products aim to improve the internal efficiency,transparency,
   accountability and the service delivery of the government  organizations.

    Copyright (C) <2015>  eGovernments Foundation

    The updated version of eGov suite of products as by eGovernments Foundation
    is available at http://www.egovernments.org

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see http://www.gnu.org/licenses/ or
    http://www.gnu.org/licenses/gpl.html .

    In addition to the terms of the GPL license to be adhered to in using this
    program, the following additional terms are to be complied with:

	1) All versions of this program, verbatim or modified must carry this
	   Legal Notice.

	2) Any misrepresentation of the origin of the material is prohibited. It
	   is required that all modified versions of this material be marked in
	   reasonable ways as different from the original version.

	3) This license does not grant any rights to any user of the program
	   with regards to rights under trademark law for use of the trade names
	   or trademarks of eGovernments Foundation.

  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.works.models.estimate;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.egov.commons.EgwStatus;
import org.egov.commons.utils.EntityType;
import org.egov.infstr.models.BaseModel;
import org.hibernate.validator.constraints.Length;

public class ProjectCode extends BaseModel implements EntityType {
  
    private static final long serialVersionUID = -1569796745047275070L;
    private String code;
    private Set<AbstractEstimate> estimates = new HashSet<AbstractEstimate>();
    private Boolean isActive;
    @Length(max = 1024, message = "projectCode.description.length")
    private String description;
    // @Required(message="projectCode.name.null")
    @Length(max = 1024, message = "projectCode.name.length")
    private String codeName;
    private EgwStatus egwStatus;
    private Double projectValue;
    private Date completionDate;

    public ProjectCode() {
    }

    public ProjectCode(final AbstractEstimate abstractEstimate, final String code) {
        estimates.add(abstractEstimate);
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public void addEstimate(final AbstractEstimate estimate) {
        estimates.add(estimate);
    }

    public Set<AbstractEstimate> getEstimates() {
        return estimates;
    }

    public void setEstimates(final Set<AbstractEstimate> estimates) {
        this.estimates = estimates;
    }

    @Override
    public String getBankaccount() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getBankname() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getIfsccode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getModeofpay() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return codeName;
    }

    @Override
    public String getPanno() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTinno() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getEntityId() {
        return Integer.valueOf(id.intValue());
    }

    @Override
    public String getEntityDescription() {
        return description;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(final Boolean isActive) {
        this.isActive = isActive;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getCodeName() {
        return codeName;
    }

    public void setCodeName(final String codeName) {
        this.codeName = codeName;
    }

    @Override
    public EgwStatus getEgwStatus() {
        return egwStatus;
    }

    public void setEgwStatus(final EgwStatus egwStatus) {
        this.egwStatus = egwStatus;
    }

    public Double getProjectValue() {
        return projectValue;
    }

    public void setProjectValue(final Double projectValue) {
        this.projectValue = projectValue;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(final Date completionDate) {
        this.completionDate = completionDate;
    }

}
