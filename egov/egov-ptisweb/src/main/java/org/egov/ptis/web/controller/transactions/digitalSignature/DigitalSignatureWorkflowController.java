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
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 ******************************************************************************/
package org.egov.ptis.web.controller.transactions.digitalSignature;

import static org.egov.ptis.constants.PropertyTaxConstants.APPLICATION_TYPE_NEW_ASSESSENT;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.egov.eis.entity.Assignment;
import org.egov.eis.service.AssignmentService;
import org.egov.infra.admin.master.entity.User;
import org.egov.infra.security.utils.SecurityUtils;
import org.egov.infra.workflow.service.SimpleWorkflowService;
import org.egov.infstr.services.PersistenceService;
import org.egov.infstr.workflow.WorkFlowMatrix;
import org.egov.pims.commons.Position;
import org.egov.ptis.domain.entity.property.BasicProperty;
import org.egov.ptis.domain.entity.property.PropertyImpl;
import org.egov.ptis.domain.service.property.PropertyService;
import org.elasticsearch.common.joda.time.DateTime;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author subhash
 *
 */
@Controller
@RequestMapping(value = "/digitalSignature")
public class DigitalSignatureWorkflowController {

    private static final String DIGITAL_SIGNATURE_SUCCESS = "digitalSignature-success";

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private SimpleWorkflowService<PropertyImpl> propertyWorkflowService;

    private PersistenceService<BasicProperty, Long> basicPropertyService;

    @RequestMapping(value = "/propertyTax/transitionWorkflow/{fileStoreIds}")
    public String transitionWorkflow(final Model model, @PathVariable final String fileStoreIds) {
        final String[] fileStoreId = fileStoreIds.split(",");
        for (final String id : fileStoreId) {
            final BasicProperty basicProperty = (BasicProperty) getCurrentSession()
                    .createQuery("select basicProperty from PtNotice where fileStore.fileStoreId = :id").setParameter("id", id)
                    .uniqueResult();
            final PropertyImpl property = basicProperty.getActiveProperty();
            transitionWorkFlow(property);
            propertyService.updateIndexes(property, APPLICATION_TYPE_NEW_ASSESSENT);
            basicPropertyService.update(basicProperty);
        }
        model.addAttribute("successMessage", "Digitally Signed Successfully");
        return DIGITAL_SIGNATURE_SUCCESS;
    }

    private void transitionWorkFlow(final PropertyImpl property) {
        final User user = securityUtils.getCurrentUser();
        final DateTime currentDate = new DateTime();
        final Position pos = getWorkflowInitiator(property).getPosition();
        final WorkFlowMatrix wfmatrix = propertyWorkflowService.getWfMatrix(property.getStateType(), null,
                null, APPLICATION_TYPE_NEW_ASSESSENT, property.getCurrentState().getValue(), null);
        property.transition(true).withSenderName(user.getName())
                .withStateValue(wfmatrix.getNextState()).withDateInfo(currentDate.toDate()).withOwner(pos)
                .withNextAction(wfmatrix.getNextAction());
    }

    private Assignment getWorkflowInitiator(final PropertyImpl property) {
        Assignment wfInitiator;
        if (propertyService.isEmployee(property.getCreatedBy()))
            wfInitiator = assignmentService.getPrimaryAssignmentForUser(property.getCreatedBy().getId());
        else if (!property.getStateHistory().isEmpty())
            wfInitiator = assignmentService.getPrimaryAssignmentForPositon(property.getStateHistory().get(0)
                    .getOwnerPosition().getId());
        else
            wfInitiator = assignmentService.getPrimaryAssignmentForPositon(property.getState().getOwnerPosition()
                    .getId());
        return wfInitiator;
    }

    private Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    public PersistenceService<BasicProperty, Long> getBasicPropertyService() {
        return basicPropertyService;
    }

    public void setBasicPropertyService(final PersistenceService<BasicProperty, Long> basicPropertyService) {
        this.basicPropertyService = basicPropertyService;
    }

}
