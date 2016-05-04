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
package org.egov.works.letterofacceptance.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.egov.commons.dao.EgwStatusHibernateDAO;
import org.egov.eis.entity.Assignment;
import org.egov.eis.service.AssignmentService;
import org.egov.eis.service.DesignationService;
import org.egov.pims.commons.Designation;
import org.egov.works.contractorbill.entity.ContractorBillRegister;
import org.egov.works.contractorbill.entity.enums.BillTypes;
import org.egov.works.letterofacceptance.entity.SearchRequestContractor;
import org.egov.works.letterofacceptance.entity.SearchRequestLetterOfAcceptance;
import org.egov.works.letterofacceptance.repository.LetterOfAcceptanceRepository;
import org.egov.works.lineestimate.entity.DocumentDetails;
import org.egov.works.lineestimate.repository.LineEstimateDetailsRepository;
import org.egov.works.lineestimate.service.LineEstimateService;
import org.egov.works.models.masters.ContractorDetail;
import org.egov.works.models.workorder.WorkOrder;
import org.egov.works.services.WorksService;
import org.egov.works.utils.WorksConstants;
import org.egov.works.utils.WorksUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class LetterOfAcceptanceService {

    @PersistenceContext
    private EntityManager entityManager;

    private final LetterOfAcceptanceRepository letterOfAcceptanceRepository;

    @Autowired
    private EgwStatusHibernateDAO egwStatusHibernateDAO;

    @Autowired
    private WorksService worksService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private DesignationService designationService;

    @Autowired
    private WorksUtils worksUtils;

    @Autowired
    private LineEstimateDetailsRepository lineEstimateDetailsRepository;

    @Autowired
    private LineEstimateService lineEstimateService;

    public Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    @Autowired
    public LetterOfAcceptanceService(final LetterOfAcceptanceRepository letterOfAcceptanceRepository) {
        this.letterOfAcceptanceRepository = letterOfAcceptanceRepository;
    }

    public WorkOrder getWorkOrderById(final Long id) {
        return letterOfAcceptanceRepository.findById(id);
    }

    public List<String> getWorkOrderByNumber(final String name) {
        final List<WorkOrder> workOrder = letterOfAcceptanceRepository
                .findByWorkOrderNumberContainingIgnoreCase(name);
        final List<String> results = new ArrayList<String>();
        for (final WorkOrder details : workOrder)
            results.add(details.getWorkOrderNumber());
        return results;
    }

    @Transactional
    public WorkOrder create(final WorkOrder workOrder, final MultipartFile[] files) throws IOException {

        workOrder.setEgwStatus(egwStatusHibernateDAO.getStatusByModuleAndCode(WorksConstants.WORKORDER,
                WorksConstants.APPROVED));

        if (StringUtils.isNotBlank(workOrder.getPercentageSign()) && workOrder.getPercentageSign().equals("-"))
            workOrder.setTenderFinalizedPercentage(workOrder.getTenderFinalizedPercentage() * -1);
        final WorkOrder savedworkOrder = letterOfAcceptanceRepository.save(workOrder);
        final List<DocumentDetails> documentDetails = worksUtils.getDocumentDetails(files, savedworkOrder,
                WorksConstants.WORKORDER);
        if (!documentDetails.isEmpty()) {
            savedworkOrder.setDocumentDetails(documentDetails);
            worksUtils.persistDocuments(documentDetails);
        }
        return savedworkOrder;
    }

    public WorkOrder getWorkOrderByWorkOrderNumber(final String workOrderNumber) {
        return letterOfAcceptanceRepository.findByWorkOrderNumberAndEgwStatus_codeNotLike(workOrderNumber,
                WorksConstants.CANCELLED_STATUS);
    }

    public String getEngineerInchargeDesignationAppConfigValue() {
        return worksService.getWorksConfigValue(WorksConstants.APPCONFIG_KEY_ENGINEERINCHARGE_DESIGNATION);
    }

    public Long getEngineerInchargeDesignationId() {
        final String engineerInchargeDesignation = getEngineerInchargeDesignationAppConfigValue();
        Designation designation = null;
        Long designationId = null;
        if (StringUtils.isNotBlank(engineerInchargeDesignation))
            designation = designationService.getDesignationByName(engineerInchargeDesignation);
        if (designation != null)
            designationId = designation.getId();
        return designationId;
    }

    public List<Assignment> getEngineerInchargeList(final Long departmentId, final Long designationId) {
        return assignmentService.getAllPositionsByDepartmentAndDesignationForGivenRange(
                departmentId, designationId, new Date());
    }

    public WorkOrder getWorkOrderByEstimateNumber(final String estimateNumber) {
        return letterOfAcceptanceRepository.findByEstimateNumberAndEgwStatus_codeNotLike(estimateNumber,
                WorksConstants.CANCELLED_STATUS);
    }

    public WorkOrder getLetterOfAcceptanceDocumentAttachments(final WorkOrder workOrder) {
        List<DocumentDetails> documentDetailsList = new ArrayList<DocumentDetails>();
        documentDetailsList = worksUtils.findByObjectIdAndObjectType(workOrder.getId(),
                WorksConstants.WORKORDER);
        workOrder.setDocumentDetails(documentDetailsList);
        return workOrder;
    }

    public WorkOrder getApprovedWorkOrder(final String workOrderNumber) {
        return letterOfAcceptanceRepository.findByWorkOrderNumberAndEgwStatus_codeEquals(workOrderNumber,
                WorksConstants.APPROVED);
    }

    public List<WorkOrder> searchLetterOfAcceptance(final SearchRequestLetterOfAcceptance searchRequestLetterOfAcceptance) {
        // TODO Need TO handle in single query
        final List<String> estimateNumbers = lineEstimateDetailsRepository
                .findEstimateNumbersForDepartment(searchRequestLetterOfAcceptance.getDepartmentName());
        if (estimateNumbers.isEmpty())
            estimateNumbers.add("");
        final Criteria criteria = entityManager.unwrap(Session.class).createCriteria(WorkOrder.class, "wo")
                .addOrder(Order.asc("workOrderDate"))
                .createAlias("wo.contractor", "woc")
                .createAlias("egwStatus", "status");
        if (searchRequestLetterOfAcceptance != null) {
            if (searchRequestLetterOfAcceptance.getWorkOrderNumber() != null)
                criteria.add(Restrictions.eq("workOrderNumber", searchRequestLetterOfAcceptance.getWorkOrderNumber()).ignoreCase());
            if (searchRequestLetterOfAcceptance.getFromDate() != null)
                criteria.add(Restrictions.ge("workOrderDate", searchRequestLetterOfAcceptance.getFromDate()));
            if (searchRequestLetterOfAcceptance.getToDate() != null)
                criteria.add(Restrictions.le("workOrderDate", searchRequestLetterOfAcceptance.getToDate()));
            if (searchRequestLetterOfAcceptance.getName() != null)
                criteria.add(Restrictions.eq("woc.name", searchRequestLetterOfAcceptance.getName()).ignoreCase());
            if (searchRequestLetterOfAcceptance.getFileNumber() != null)
                criteria.add(
                        Restrictions.ilike("fileNumber", searchRequestLetterOfAcceptance.getFileNumber(), MatchMode.ANYWHERE));
            if (searchRequestLetterOfAcceptance.getEstimateNumber() != null)
                criteria.add(Restrictions.eq("estimateNumber", searchRequestLetterOfAcceptance.getEstimateNumber()).ignoreCase());
            if (searchRequestLetterOfAcceptance.getDepartmentName() != null)
                criteria.add(Restrictions.in("estimateNumber", estimateNumbers));
            if (searchRequestLetterOfAcceptance.getEgwStatus() != null)
                criteria.add(Restrictions.eq("status.code", searchRequestLetterOfAcceptance.getEgwStatus()));
        }
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }

    public List<WorkOrder> searchLetterOfAcceptanceForContractorBill(
            final SearchRequestLetterOfAcceptance searchRequestLetterOfAcceptance) {
        final List<String> estimateNumbers = lineEstimateService
                .getEstimateNumberForDepartment(searchRequestLetterOfAcceptance.getDepartmentName());
        if (estimateNumbers.isEmpty())
            estimateNumbers.add("");
        // TODO: replace fetching workorders by query with criteria alias
        final List<String> workOrderNumbers = letterOfAcceptanceRepository.getDistinctNonCancelledWorkOrderNumbersByBillType(
                ContractorBillRegister.BillStatus.CANCELLED.toString(), BillTypes.Final_Bill.toString());
        final Criteria criteria = entityManager.unwrap(Session.class).createCriteria(WorkOrder.class, "wo")
                .createAlias("contractor", "woc")
                .createAlias("egwStatus", "status");

        if (searchRequestLetterOfAcceptance != null) {
            if (searchRequestLetterOfAcceptance.getWorkOrderNumber() != null)
                criteria.add(Restrictions.eq("workOrderNumber", searchRequestLetterOfAcceptance.getWorkOrderNumber()).ignoreCase());
            if (searchRequestLetterOfAcceptance.getFromDate() != null)
                criteria.add(Restrictions.ge("workOrderDate", searchRequestLetterOfAcceptance.getFromDate()));
            if (searchRequestLetterOfAcceptance.getToDate() != null)
                criteria.add(Restrictions.le("workOrderDate", searchRequestLetterOfAcceptance.getToDate()));
            if (searchRequestLetterOfAcceptance.getName() != null)
                criteria.add(Restrictions.eq("woc.name", searchRequestLetterOfAcceptance.getName()).ignoreCase());
            if (searchRequestLetterOfAcceptance.getFileNumber() != null)
            criteria.add(Restrictions.ilike("fileNumber", searchRequestLetterOfAcceptance.getFileNumber(), MatchMode.ANYWHERE));
            if (searchRequestLetterOfAcceptance.getEstimateNumber() != null)
                criteria.add(Restrictions.eq("estimateNumber", searchRequestLetterOfAcceptance.getEstimateNumber()).ignoreCase());
            if (searchRequestLetterOfAcceptance.getDepartmentName() != null)
                criteria.add(Restrictions.in("estimateNumber", estimateNumbers));
            if (workOrderNumbers != null && !workOrderNumbers.isEmpty())
                criteria.add(Restrictions.not(Restrictions.in("workOrderNumber", workOrderNumbers)));
        }
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }

    public List<String> findLoaEstimateNumbers(final String name) {
        final List<WorkOrder> workorders = letterOfAcceptanceRepository.findByEstimateNumberContainingIgnoreCase(name);
        final List<String> results = new ArrayList<String>();
        for (final WorkOrder details : workorders)
            results.add(details.getEstimateNumber());
        return results;
    }

    public List<String> findDistinctContractorsInWorkOrderByCodeOrName(final String name) {
        final List<String> results = letterOfAcceptanceRepository
                .findDistinctContractorByContractor_codeAndNameContainingIgnoreCase("%" + name + "%");
        return results;
    }

    public List<String> findLoaEstimateNumbersForContractorBill(final String estimateNumber) {
        final List<WorkOrder> workorders = letterOfAcceptanceRepository
                .findByEstimateNumberAndEgwStatus_codeEquals(estimateNumber, WorksConstants.APPROVED);
        final List<String> results = new ArrayList<String>();
        for (final WorkOrder details : workorders)
            results.add(details.getEstimateNumber());
        return results;
    }

    public List<String> getApprovedWorkOrdersForCreateContractorBill(final String workOrderNumber) {
        final List<String> results = letterOfAcceptanceRepository.findWorkOrderNumberForContractorBill(
                "%" + workOrderNumber + "%", WorksConstants.APPROVED, ContractorBillRegister.BillStatus.CANCELLED.toString(),
                BillTypes.Final_Bill.toString());
        return results;

    }

    public List<String> getApprovedEstimateNumbersForCreateContractorBill(final String estimateNumber) {
        final List<String> results = letterOfAcceptanceRepository.findEstimateNumberForContractorBill("%" + estimateNumber + "%",
                WorksConstants.APPROVED, ContractorBillRegister.BillStatus.CANCELLED.toString(), BillTypes.Final_Bill.toString());
        return results;
    }

    public List<String> getApprovedContractorsForCreateContractorBill(final String contractorname) {
        final List<String> results = letterOfAcceptanceRepository.findContractorForContractorBill("%" + contractorname + "%",
                WorksConstants.APPROVED, ContractorBillRegister.BillStatus.CANCELLED.toString(), BillTypes.Final_Bill.toString());
        return results;
    }

    public Boolean validateContractorBillInWorkflowForWorkorder(final Long workOrderId) {
        final List<String> results = letterOfAcceptanceRepository.getContractorBillInWorkflowForWorkorder(workOrderId,
                ContractorBillRegister.BillStatus.CANCELLED.toString(), ContractorBillRegister.BillStatus.APPROVED.toString());
        if (results.isEmpty())
            return true;
        else
            return false;
    }
    
    public List<ContractorDetail> searchContractorDetails(final SearchRequestContractor searchRequestContractor) {
        final Criteria criteria = entityManager.unwrap(Session.class).createCriteria(ContractorDetail.class, "cd")
                .createAlias("contractor", "contractor");
        if (searchRequestContractor != null) {
            if (searchRequestContractor.getDepartment() != null)
                criteria.add(Restrictions.eq("department.id", searchRequestContractor.getDepartment()));
            if (searchRequestContractor.getContractorClass() != null)
                criteria.add(Restrictions.ge("grade.id", searchRequestContractor.getContractorClass()));
            if (searchRequestContractor.getContractorCode() != null)
                criteria.add(Restrictions.eq("contractor.code", searchRequestContractor.getContractorCode()).ignoreCase());
            if (searchRequestContractor.getNameOfAgency() != null)
                criteria.add(Restrictions.ilike("contractor.name", searchRequestContractor.getNameOfAgency(), MatchMode.ANYWHERE));
        }
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }
    
    public List<String> findLoaWorkOrderNumberForMilestone(final String workOrderNumber) {
        final List<WorkOrder> workorders = letterOfAcceptanceRepository
                .findByWorkOrderNumberContainingIgnoreCaseAndEgwStatus_codeEquals(workOrderNumber, WorksConstants.APPROVED);
        final List<String> results = new ArrayList<String>();
        for (final WorkOrder details : workorders)
            results.add(details.getWorkOrderNumber());
        return results;
    }
    
    public List<String> findWorkIdentificationNumbersToCreateMilestone(final String code) {
        final List<String> workIdNumbers = letterOfAcceptanceRepository
                .findWorkIdentificationNumberToCreateMilestone("%" + code + "%");
        return workIdNumbers;
    }
    
    public List<WorkOrder> getLoaForCreateMilestone(SearchRequestLetterOfAcceptance searchRequestLetterOfAcceptance) {
        final StringBuilder queryStr = new StringBuilder(500);
        
            buildWhereClause(searchRequestLetterOfAcceptance, queryStr);
            final Query query = setParameterForMilestone(searchRequestLetterOfAcceptance, queryStr);
            final List<WorkOrder> workOrderList = query.getResultList();
            return workOrderList;      
    }
    
    private void buildWhereClause(SearchRequestLetterOfAcceptance searchRequestLetterOfAcceptance, final StringBuilder queryStr) {
        queryStr.append("select distinct wo from WorkOrder wo where wo.egwStatus.moduletype = :moduleType and wo.egwStatus.code = :status ");
        queryStr.append(" and wo.estimateNumber in (select led.estimateNumber from LineEstimateDetails led where led.lineEstimate.executingDepartment.id = :departmentName)");
        
        if (StringUtils.isNotBlank(searchRequestLetterOfAcceptance.getWorkIdentificationNumber()))
            queryStr.append(" and wo.estimateNumber = (select led.estimateNumber from LineEstimateDetails led where led.projectCode = (select po.id from ProjectCode po where upper(po.code) = :workIdentificationNumber))");

        if (StringUtils.isNotBlank(searchRequestLetterOfAcceptance.getEstimateNumber()))
            queryStr.append(" and upper(wo.estimateNumber) = :estimateNumber");
           
        if (StringUtils.isNotBlank(searchRequestLetterOfAcceptance.getWorkOrderNumber()))
            queryStr.append(" and upper(wo.workOrderNumber) = :workOrderNumber");

        if (searchRequestLetterOfAcceptance.getTypeOfWork() != null)
            queryStr.append(" and wo.estimateNumber in (select led.estimateNumber from LineEstimateDetails led where led.lineEstimate.typeOfWork = :typeOfWork)");

        if (searchRequestLetterOfAcceptance.getSubTypeOfWork() != null)
            queryStr.append(" and wo.estimateNumber in (select led.estimateNumber from LineEstimateDetails led where led.lineEstimate.subTypeOfWork = subTypeOfWork)");
        
        if (searchRequestLetterOfAcceptance.getAdminSanctionFromDate() != null)
            queryStr.append(" and wo.estimateNumber in (select led.estimateNumber from LineEstimateDetails led where led.lineEstimate.adminSanctionDate >= :adminSanctionFromDate)");

        if (searchRequestLetterOfAcceptance.getAdminSanctionToDate() != null)
            queryStr.append(" and wo.estimateNumber in (select led.estimateNumber from LineEstimateDetails led where led.lineEstimate.adminSanctionDate <= :adminSanctionFromDate)");
    
    }

    private Query setParameterForMilestone(SearchRequestLetterOfAcceptance searchRequestLetterOfAcceptance, final StringBuilder queryStr) {
        final Query qry = entityManager.createQuery(queryStr.toString());
            qry.setParameter("status", WorksConstants.APPROVED);
            qry.setParameter("moduleType", WorksConstants.WORKORDER);
        if (searchRequestLetterOfAcceptance != null ) {
            qry.setParameter("departmentName", searchRequestLetterOfAcceptance.getDepartmentName());
        if (StringUtils.isNotBlank(searchRequestLetterOfAcceptance.getWorkIdentificationNumber()))
            qry.setParameter("workIdentificationNumber", searchRequestLetterOfAcceptance.getWorkIdentificationNumber().toUpperCase());
        if (StringUtils.isNotBlank(searchRequestLetterOfAcceptance.getEstimateNumber()))
            qry.setParameter("estimateNumber", searchRequestLetterOfAcceptance.getEstimateNumber().toUpperCase());
        if (StringUtils.isNotBlank(searchRequestLetterOfAcceptance.getWorkOrderNumber()))
            qry.setParameter("workOrderNumber", searchRequestLetterOfAcceptance.getWorkOrderNumber().toUpperCase());
        if (searchRequestLetterOfAcceptance.getTypeOfWork() != null)
            qry.setParameter("typeOfWork", searchRequestLetterOfAcceptance.getTypeOfWork());
        if (searchRequestLetterOfAcceptance.getSubTypeOfWork() != null)
            qry.setParameter("subTypeOfWork", searchRequestLetterOfAcceptance.getSubTypeOfWork());
        if (searchRequestLetterOfAcceptance.getAdminSanctionFromDate() != null)
            qry.setParameter("adminSanctionFromDate", searchRequestLetterOfAcceptance.getAdminSanctionFromDate());
        if (searchRequestLetterOfAcceptance.getAdminSanctionToDate() != null)
            qry.setParameter("adminSanctionToDate", searchRequestLetterOfAcceptance.getAdminSanctionToDate());

        }
        return qry;
    }
}