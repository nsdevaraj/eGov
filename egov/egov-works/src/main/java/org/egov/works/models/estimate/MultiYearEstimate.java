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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.Min;

import org.egov.commons.CFinancialYear;
import org.egov.infstr.ValidationError;
import org.egov.infstr.models.BaseModel;

public class MultiYearEstimate extends BaseModel {

    private static final long serialVersionUID = -7118385910723277266L;
    private AbstractEstimate abstractEstimate;
    private CFinancialYear financialYear;
    @Min(value = 0, message = "multiYeareEstimate.percentage.not.negative")
    private double percentage;

    public MultiYearEstimate() {
    }

    public MultiYearEstimate(final AbstractEstimate abstractEstimate, final CFinancialYear financialYear,
            final double percentage) {
        this.abstractEstimate = abstractEstimate;
        this.financialYear = financialYear;
        this.percentage = percentage;
    }

    public AbstractEstimate getAbstractEstimate() {
        return abstractEstimate;
    }

    public void setAbstractEstimate(final AbstractEstimate abstractEstimate) {
        this.abstractEstimate = abstractEstimate;
    }

    public CFinancialYear getFinancialYear() {
        return financialYear;
    }

    public void setFinancialYear(final CFinancialYear financialYear) {
        this.financialYear = financialYear;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(final double percentage) {
        this.percentage = percentage;
    }

    @Override
    public List<ValidationError> validate() {
        if (percentage < 0.0)
            return Arrays.asList(new ValidationError("percentage",
                    "multiYeareEstimate.percentage.percentage_greater_than_0"));
        if (percentage > 100.0)
            return Arrays.asList(new ValidationError("percentage",
                    "multiYeareEstimate.percentage.percentage_less_than_100"));
        return new ArrayList<ValidationError>();
    }
}
