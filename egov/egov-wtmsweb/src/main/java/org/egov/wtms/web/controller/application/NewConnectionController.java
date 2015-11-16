/**
 * eGov suite of products aim to improve the internal efficiency,transparency, accountability and the service delivery of the
 * government organizations.
 *
 * Copyright (C) <2015> eGovernments Foundation
 *
 * The updated version of eGov suite of products as by eGovernments Foundation is available at http://www.egovernments.org
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/ or http://www.gnu.org/licenses/gpl.html .
 *
 * In addition to the terms of the GPL license to be adhered to in using this program, the following additional terms are to be
 * complied with:
 *
 * 1) All versions of this program, verbatim or modified must carry this Legal Notice.
 *
 * 2) Any misrepresentation of the origin of the material is prohibited. It is required that all modified versions of this
 * material be marked in reasonable ways as different from the original version.
 *
 * 3) This license does not grant any rights to any user of the program with regards to rights under trademark law for use of the
 * trade names or trademarks of eGovernments Foundation.
 *
 * In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.wtms.web.controller.application;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.ArrayUtils;
import org.egov.eis.web.contract.WorkflowContainer;
import org.egov.infra.exception.ApplicationRuntimeException;
import org.egov.infra.security.utils.SecurityUtils;
import org.egov.infra.utils.ApplicationNumberGenerator;
import org.egov.pims.commons.Position;
import org.egov.wtms.application.entity.ApplicationDocuments;
import org.egov.wtms.application.entity.WaterConnectionDetails;
import org.egov.wtms.application.service.ConnectionDemandService;
import org.egov.wtms.application.service.NewConnectionService;
import org.egov.wtms.application.service.WaterConnectionDetailsService;
import org.egov.wtms.masters.entity.ConnectionCategory;
import org.egov.wtms.masters.entity.DocumentNames;
import org.egov.wtms.masters.entity.enums.ConnectionStatus;
import org.egov.wtms.masters.entity.enums.ConnectionType;
import org.egov.wtms.masters.service.ApplicationTypeService;
import org.egov.wtms.utils.WaterTaxUtils;
import org.egov.wtms.utils.constants.WaterTaxConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping(value = "/application")
public class NewConnectionController extends GenericConnectionController {
    private static final Logger LOG = LoggerFactory.getLogger(NewConnectionController.class);
    private final WaterConnectionDetailsService waterConnectionDetailsService;
    private final ApplicationTypeService applicationTypeService;
    private final ConnectionDemandService connectionDemandService;
    private final NewConnectionService newConnectionService;
    private final WaterTaxUtils waterTaxUtils;
    @Autowired
    private SecurityUtils securityUtils;
    private Boolean loggedUserIsMeesevaUser = Boolean.FALSE;
    @Autowired
    private ApplicationNumberGenerator applicationNumberGenerator;

    @Autowired
    public NewConnectionController(final WaterConnectionDetailsService waterConnectionDetailsService,
            final ApplicationTypeService applicationTypeService, final ConnectionDemandService connectionDemandService,
            final WaterTaxUtils waterTaxUtils, final NewConnectionService newConnectionService,
            final SmartValidator validator) {
        this.waterConnectionDetailsService = waterConnectionDetailsService;
        this.applicationTypeService = applicationTypeService;
        this.connectionDemandService = connectionDemandService;
        this.waterTaxUtils = waterTaxUtils;
        this.newConnectionService = newConnectionService;

    }

    public @ModelAttribute("documentNamesList") List<DocumentNames> documentNamesList(
            @ModelAttribute final WaterConnectionDetails waterConnectionDetails) {
        waterConnectionDetails.setApplicationType(applicationTypeService.findByCode(WaterTaxConstants.NEWCONNECTION));
        return waterConnectionDetailsService.getAllActiveDocumentNames(waterConnectionDetails.getApplicationType());
    }

    @RequestMapping(value = "/newConnection-newform", method = GET)
    public String showNewApplicationForm(@ModelAttribute final WaterConnectionDetails waterConnectionDetails,
            final Model model, final HttpServletRequest request) {
        waterConnectionDetails.setApplicationDate(new Date());
        waterConnectionDetails.setConnectionStatus(ConnectionStatus.INPROGRESS);
        model.addAttribute("allowIfPTDueExists", waterTaxUtils.isNewConnectionAllowedIfPTDuePresent());
        model.addAttribute("additionalRule", waterConnectionDetails.getApplicationType().getCode());
        prepareWorkflow(model, waterConnectionDetails, new WorkflowContainer());
        model.addAttribute("currentUser", waterTaxUtils.getCurrentUserRole(securityUtils.getCurrentUser()));
        model.addAttribute("stateType", waterConnectionDetails.getClass().getSimpleName());
        model.addAttribute("documentName", waterTaxUtils.documentRequiredForBPLCategory());
        model.addAttribute("typeOfConnection", WaterTaxConstants.NEWCONNECTION);

        loggedUserIsMeesevaUser = waterTaxUtils.isMeesevaUser(securityUtils.getCurrentUser());
        if (loggedUserIsMeesevaUser)
            if (request.getParameter("applicationNo") == null)
                throw new ApplicationRuntimeException("mandatory.meesevaApplicationNumber");
            else
                waterConnectionDetails.setMeesevaApplicationNumber(request.getParameter("applicationNo"));
        return "newconnection-form";
    }

    @RequestMapping(value = "/newConnection-dataEntryForm", method = GET)
    public String dataEntryForm(@ModelAttribute final WaterConnectionDetails waterConnectionDetails, final Model model) {
        waterConnectionDetails.setApplicationDate(new Date());
        waterConnectionDetails.setConnectionStatus(ConnectionStatus.ACTIVE);
        final Map<Long, String> connectionTypeMap = new HashMap<Long, String>();
        connectionTypeMap.put(applicationTypeService.findByCode(WaterTaxConstants.NEWCONNECTION).getId(),
                WaterTaxConstants.PRIMARYCONNECTION);
        connectionTypeMap.put(applicationTypeService.findByCode(WaterTaxConstants.ADDNLCONNECTION).getId(),
                WaterTaxConstants.CONN_NAME_ADDNLCONNECTION);
        model.addAttribute("radioButtonMap", connectionTypeMap);
        model.addAttribute("mode", "dataEntry");
        model.addAttribute("typeOfConnection", WaterTaxConstants.NEWCONNECTION);
        return "newconnection-dataEntryForm";
    }

    @RequestMapping(value = "/newConnection-existingMessage/{consumerCode}", method = GET)
    public String dataEntryMessage(final Model model, @PathVariable final String consumerCode) {
        model.addAttribute("consumerCode", consumerCode);
        final WaterConnectionDetails waterConnectionDetails = waterConnectionDetailsService
                .findByApplicationNumberOrConsumerCode(consumerCode);
        model.addAttribute(
                "connectionType",
                waterConnectionDetailsService.getConnectionTypesMap().get(
                        waterConnectionDetails.getConnectionType().name()));
        return "newconnection-dataEntryMessage";
    }

    @RequestMapping(value = "/newConnection-create", method = POST)
    public String createNewConnection(@Valid @ModelAttribute final WaterConnectionDetails waterConnectionDetails,
            final BindingResult resultBinder, final RedirectAttributes redirectAttributes,
            final HttpServletRequest request, final Model model, @RequestParam String workFlowAction,
            final BindingResult errors) {

        validatePropertyID(waterConnectionDetails, resultBinder);

        final List<ApplicationDocuments> applicationDocs = new ArrayList<ApplicationDocuments>();
        int i = 0;
        final String documentRequired = waterTaxUtils.documentRequiredForBPLCategory();
        if (!waterConnectionDetails.getApplicationDocs().isEmpty())
            for (final ApplicationDocuments applicationDocument : waterConnectionDetails.getApplicationDocs()) {
                validateDocuments(applicationDocs, applicationDocument, i, resultBinder, waterConnectionDetails
                        .getCategory().getId(), documentRequired);
                i++;
            }
        if (waterConnectionDetails.getState() == null)
            waterConnectionDetails.setStatus(waterTaxUtils.getStatusByCodeAndModuleType(
                    WaterTaxConstants.APPLICATION_STATUS_CREATED, WaterTaxConstants.MODULETYPE));
        if (LOG.isDebugEnabled())
            LOG.debug("Model Level Validation occurs = " + resultBinder);
        if (resultBinder.hasErrors()) {
            model.addAttribute("validateIfPTDueExists", waterTaxUtils.isNewConnectionAllowedIfPTDuePresent());
            prepareWorkflow(model, waterConnectionDetails, new WorkflowContainer());
            model.addAttribute("additionalRule", waterConnectionDetails.getApplicationType().getCode());
            model.addAttribute("currentUser", waterTaxUtils.getCurrentUserRole(securityUtils.getCurrentUser()));
            model.addAttribute("stateType", waterConnectionDetails.getClass().getSimpleName());
            return "newconnection-form";
        }

        waterConnectionDetails.getApplicationDocs().clear();
        waterConnectionDetails.setApplicationDocs(applicationDocs);

        processAndStoreApplicationDocuments(waterConnectionDetails);

        Long approvalPosition = 0l;
        String approvalComent = "";

        if (request.getParameter("approvalComent") != null)
            approvalComent = request.getParameter("approvalComent");
        if (request.getParameter("workFlowAction") != null)
            workFlowAction = request.getParameter("workFlowAction");
        if (request.getParameter("approvalPosition") != null && !request.getParameter("approvalPosition").isEmpty())
            approvalPosition = Long.valueOf(request.getParameter("approvalPosition"));
        final Boolean applicationByOthers = waterTaxUtils.getCurrentUserRole(securityUtils.getCurrentUser());

        if (applicationByOthers != null && applicationByOthers.equals(true)) {
            final Position userPosition = waterTaxUtils.getZonalLevelClerkForLoggedInUser(waterConnectionDetails
                    .getConnection().getPropertyIdentifier());
            if (userPosition == null) {
                model.addAttribute("validateIfPTDueExists", waterTaxUtils.isNewConnectionAllowedIfPTDuePresent());
                prepareWorkflow(model, waterConnectionDetails, new WorkflowContainer());
                model.addAttribute("additionalRule", waterConnectionDetails.getApplicationType().getCode());
                model.addAttribute("currentUser", waterTaxUtils.getCurrentUserRole(securityUtils.getCurrentUser()));
                model.addAttribute("stateType", waterConnectionDetails.getClass().getSimpleName());
                errors.rejectValue("connection.propertyIdentifier", "err.validate.connection.user.mapping",
                        "err.validate.connection.user.mapping");
                return "newconnection-form";
            } else
                approvalPosition = userPosition.getId();

        }

        loggedUserIsMeesevaUser = waterTaxUtils.isMeesevaUser(securityUtils.getCurrentUser());
        if (loggedUserIsMeesevaUser) {
            final HashMap<String, String> meesevaParams = new HashMap<String, String>();
            meesevaParams.put("APPLICATIONNUMBER", waterConnectionDetails.getMeesevaApplicationNumber());
            if (waterConnectionDetails.getApplicationNumber() == null) {
                waterConnectionDetails.setApplicationNumber(applicationNumberGenerator.generate());
                waterConnectionDetailsService.createNewWaterConnection(waterConnectionDetails, approvalPosition,
                        approvalComent, waterConnectionDetails.getApplicationType().getCode(), workFlowAction,
                        meesevaParams);
            }
        } else
            waterConnectionDetailsService.createNewWaterConnection(waterConnectionDetails, approvalPosition,
                    approvalComent, waterConnectionDetails.getApplicationType().getCode(), workFlowAction);

        if (LOG.isDebugEnabled())
            LOG.debug("createNewWaterConnection is completed ");
        final String pathVars = waterConnectionDetails.getApplicationNumber() + ","
                + waterTaxUtils.getApproverUserName(approvalPosition);
        if (loggedUserIsMeesevaUser)
            return "redirect:/application/generate-meesevareceipt?transactionServiceNumber=" + pathVars;
        else
            return "redirect:/application/application-success?pathVars=" + pathVars;

        // return "redirect:/application/application-success?pathVars=" +
        // pathVars;
    }

    @ModelAttribute
    public WaterConnectionDetails loadByConsumerNo(@RequestParam(name = "id", required = false) final Long id) {
        if (id != null)
            return waterConnectionDetailsService.findBy(id);
        else
            return new WaterConnectionDetails();
    }

    // used to create/update existing details
    @RequestMapping(value = "/newConnection-createExisting", method = POST)
    public String createExisting(@Valid @ModelAttribute final WaterConnectionDetails waterConnectionDetails,
            final BindingResult resultBinder, final RedirectAttributes redirectAttributes,
            final HttpServletRequest request, final Model model) {

        validatePropertyIDForDataEntry(waterConnectionDetails, resultBinder);
        validateExisting(waterConnectionDetails, resultBinder);
        if (resultBinder.hasErrors()) {
            model.addAttribute("validateIfPTDueExists", waterTaxUtils.isNewConnectionAllowedIfPTDuePresent());
            final Map<Long, String> connectionTypeMap = new HashMap<Long, String>();

            connectionTypeMap.put(applicationTypeService.findByCode(WaterTaxConstants.NEWCONNECTION).getId(),
                    WaterTaxConstants.PRIMARYCONNECTION);
            connectionTypeMap.put(applicationTypeService.findByCode(WaterTaxConstants.ADDNLCONNECTION).getId(),
                    WaterTaxConstants.CONN_NAME_ADDNLCONNECTION);
            model.addAttribute("radioButtonMap", connectionTypeMap);
            if (waterConnectionDetails.getId() == null)
                return "newconnection-dataEntryForm";
            else
                return "newconnection-dataEntryEditForm";
        }
        waterConnectionDetailsService.createExisting(waterConnectionDetails);
        return "redirect:newConnection-existingMessage/" + waterConnectionDetails.getConnection().getConsumerCode();
    }

    private void validateDocuments(final List<ApplicationDocuments> applicationDocs,
            final ApplicationDocuments applicationDocument, final int i, final BindingResult resultBinder,
            final Long categoryId, final String documentRequired) {

        final ConnectionCategory connectionCategory = connectionCategoryService.findBy(categoryId);
        if (connectionCategory != null && documentRequired != null
                && connectionCategory.getCode().equalsIgnoreCase(WaterTaxConstants.CATEGORY_BPL)
                && documentRequired.equalsIgnoreCase(applicationDocument.getDocumentNames().getDocumentName())) {

            if (applicationDocument.getDocumentNumber() == null) {
                final String fieldError = "applicationDocs[" + i + "].documentNumber";
                resultBinder.rejectValue(fieldError, "documentNumber.required");
            }
            if (applicationDocument.getDocumentDate() == null) {
                final String fieldError = "applicationDocs[" + i + "].documentDate";
                resultBinder.rejectValue(fieldError, "documentDate.required");
            }

            Iterator<MultipartFile> stream = null;
            if (ArrayUtils.isNotEmpty(applicationDocument.getFiles()))
                stream = Arrays.asList(applicationDocument.getFiles()).stream().filter(file -> !file.isEmpty())
                .iterator();
            if (ArrayUtils.isEmpty(applicationDocument.getFiles()) || stream == null || stream != null
                    && !stream.hasNext()) {
                final String fieldError = "applicationDocs[" + i + "].files";
                resultBinder.rejectValue(fieldError, "files.required");
            } else if (validApplicationDocument(applicationDocument))
                applicationDocs.add(applicationDocument);
        } else {
            if (applicationDocument.getDocumentNumber() == null && applicationDocument.getDocumentDate() != null) {
                final String fieldError = "applicationDocs[" + i + "].documentNumber";
                resultBinder.rejectValue(fieldError, "documentNumber.required");
            }
            if (applicationDocument.getDocumentNumber() != null && applicationDocument.getDocumentDate() == null) {
                final String fieldError = "applicationDocs[" + i + "].documentDate";
                resultBinder.rejectValue(fieldError, "documentDate.required");
            } else if (validApplicationDocument(applicationDocument))
                applicationDocs.add(applicationDocument);
        }
    }

    @RequestMapping(value = "/generate-meesevareceipt", method = GET)
    public RedirectView generateMeesevaReceipt(@ModelAttribute final WaterConnectionDetails waterConnectionDetails,
            final HttpServletRequest request, final Model model) {
        final String keyNameArray = request.getParameter("transactionServiceNumber");

        final RedirectView redirect = new RedirectView(WaterTaxConstants.MEESEVA_REDIRECT_URL + keyNameArray, false);
        final FlashMap outputFlashMap = RequestContextUtils.getOutputFlashMap(request);
        if (outputFlashMap != null)
            outputFlashMap.put("url", request.getRequestURL());
        return redirect;
    }

    @RequestMapping(value = "/application-success", method = GET)
    public ModelAndView successView(@ModelAttribute WaterConnectionDetails waterConnectionDetails,
            final HttpServletRequest request, final Model model) {

        final String[] keyNameArray = request.getParameter("pathVars").split(",");
        String applicationNumber = "";
        String approverName = "";
        if (keyNameArray.length != 0 && keyNameArray.length > 0)
            if (keyNameArray.length == 1)
                applicationNumber = keyNameArray[0];
            else {
                applicationNumber = keyNameArray[0];
                approverName = keyNameArray[1];
            }
        if (applicationNumber != null)
            waterConnectionDetails = waterConnectionDetailsService.findByApplicationNumber(applicationNumber);
        model.addAttribute("approverName", approverName);
        model.addAttribute(
                "connectionType",
                waterConnectionDetailsService.getConnectionTypesMap().get(
                        waterConnectionDetails.getConnectionType().name()));
        model.addAttribute("cityName", waterTaxUtils.getCityName());
        model.addAttribute("applicationDocList",
                waterConnectionDetailsService.getApplicationDocForExceptClosureAndReConnection(waterConnectionDetails));
        model.addAttribute("feeDetails", connectionDemandService.getSplitFee(waterConnectionDetails));
        model.addAttribute("mode", "ack");
        return new ModelAndView("application/application-success", "waterConnectionDetails", waterConnectionDetails);

    }

    private void validatePropertyID(final WaterConnectionDetails waterConnectionDetails, final BindingResult errors) {
        if (waterConnectionDetails.getConnection() != null
                && waterConnectionDetails.getConnection().getPropertyIdentifier() != null
                && !waterConnectionDetails.getConnection().getPropertyIdentifier().equals("")) {
            String errorMessage = newConnectionService.checkValidPropertyAssessmentNumber(waterConnectionDetails
                    .getConnection().getPropertyIdentifier());
            if (errorMessage != null && !errorMessage.equals(""))
                errors.rejectValue("connection.propertyIdentifier", errorMessage, errorMessage);
            else {
                errorMessage = newConnectionService.checkConnectionPresentForProperty(waterConnectionDetails
                        .getConnection().getPropertyIdentifier());
                if (errorMessage != null && !errorMessage.equals(""))
                    errors.rejectValue("connection.propertyIdentifier", errorMessage, errorMessage);
            }
        }
    }

    private void validatePropertyIDForDataEntry(final WaterConnectionDetails waterConnectionDetails,
            final BindingResult errors) {
        if (waterConnectionDetails.getConnection() != null
                && waterConnectionDetails.getConnection().getPropertyIdentifier() != null
                && !waterConnectionDetails.getConnection().getPropertyIdentifier().equals("")) {
            String errorMessage = newConnectionService.checkValidPropertyForDataEntry(waterConnectionDetails
                    .getConnection().getPropertyIdentifier());
            if (errorMessage != null && !errorMessage.equals(""))
                errors.rejectValue("connection.propertyIdentifier", errorMessage, errorMessage);
            else // if it is not edit mode then only validate for existing
                // connection
            if (waterConnectionDetails.getId() == null)
                    if (waterConnectionDetails.getApplicationType().getCode()
                        .equalsIgnoreCase(WaterTaxConstants.NEWCONNECTION)) {
                        errorMessage = newConnectionService.checkConnectionPresentForProperty(waterConnectionDetails
                                .getConnection().getPropertyIdentifier());
                        if (errorMessage != null && !errorMessage.equals(""))
                            errors.rejectValue("connection.propertyIdentifier", errorMessage, errorMessage);
                    }
        }
    }

    private void validateExisting(final WaterConnectionDetails waterConnectionDetails, final BindingResult errors) {

        if (waterConnectionDetails.getExistingConnection().getArrears() == null)
            errors.rejectValue("existingConnection.arrears", "err.required");
        if (waterConnectionDetails.getExistingConnection().getDonationCharges() == null)
            errors.rejectValue("existingConnection.donationCharges", "err.required");

        if (waterConnectionDetails.getConnectionType() != null
                && waterConnectionDetails.getConnectionType() == ConnectionType.METERED) {
            if (waterConnectionDetails.getExistingConnection().getMeterCost() == null)
                errors.rejectValue("existingConnection.meterCost", "err.required");
            if (waterConnectionDetails.getConnection().getConsumerCode() == null)
                errors.rejectValue("connection.consumerCode", "err.required");
            if (waterConnectionDetails.getConnection().getConsumerCode() != null) {
                final WaterConnectionDetails validateExistWaterConnectionDet = waterConnectionDetailsService
                        .findByApplicationNumberOrConsumerCode(waterConnectionDetails.getConnection().getConsumerCode());
                if (validateExistWaterConnectionDet != null)
                    errors.rejectValue("connection.consumerCode", "err.exist.consumerCode");
            }
            if (waterConnectionDetails.getExecutionDate() == null)
                errors.rejectValue("executionDate", "err.required");
            if (waterConnectionDetails.getExistingConnection().getMeterName() == null)
                errors.rejectValue("existingConnection.meterName", "err.required");
            if (waterConnectionDetails.getExistingConnection().getMeterNo() == null)
                errors.rejectValue("existingConnection.meterNo", "err.required");
            if (waterConnectionDetails.getExistingConnection().getPreviousReading() == null)
                errors.rejectValue("existingConnection.previousReading", "err.required");
            if (waterConnectionDetails.getExistingConnection().getReadingDate() == null)
                errors.rejectValue("existingConnection.readingDate", "err.required");
            if (waterConnectionDetails.getExistingConnection().getCurrentReading() == null)
                errors.rejectValue("existingConnection.currentReading", "err.required");

        } else if (waterConnectionDetails.getExistingConnection().getMonthlyFee() == null)
            errors.rejectValue("existingConnection.monthlyFee", "err.required");
    }

    @RequestMapping(value = "/newConnection-editExisting/{consumerCode}", method = GET)
    public String editExisting(@ModelAttribute WaterConnectionDetails waterConnectionDetails,
            @PathVariable final String consumerCode, final Model model) {
        waterConnectionDetails = waterConnectionDetailsService.findByApplicationNumberOrConsumerCode(consumerCode);
        model.addAttribute("allowIfPTDueExists", waterTaxUtils.isNewConnectionAllowedIfPTDuePresent());
        model.addAttribute("additionalRule", waterConnectionDetails.getApplicationType().getCode());
        model.addAttribute("currentUser", waterTaxUtils.getCurrentUserRole(securityUtils.getCurrentUser()));
        model.addAttribute("stateType", waterConnectionDetails.getClass().getSimpleName());
        final Map<Long, String> connectionTypeMap = new HashMap<Long, String>();

        connectionTypeMap.put(applicationTypeService.findByCode(WaterTaxConstants.NEWCONNECTION).getId(),
                WaterTaxConstants.PRIMARYCONNECTION);
        connectionTypeMap.put(applicationTypeService.findByCode(WaterTaxConstants.ADDNLCONNECTION).getId(),
                WaterTaxConstants.CONN_NAME_ADDNLCONNECTION);
        model.addAttribute("radioButtonMap", connectionTypeMap);
        model.addAttribute("mode", "dataEntry");
        model.addAttribute("waterConnectionDetails", waterConnectionDetails);
        return "newconnection-dataEntryEditForm";
    }
}
