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
package org.egov.wtms.application.service.collection;

import static org.egov.ptis.constants.PropertyTaxConstants.BILLTYPE_MANUAL;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.collection.entity.ReceiptDetail;
import org.egov.collection.integration.models.BillAccountDetails;
import org.egov.collection.integration.models.BillDetails;
import org.egov.collection.integration.models.BillInfo.COLLECTIONTYPE;
import org.egov.collection.integration.models.BillInfoImpl;
import org.egov.collection.integration.models.BillPayeeDetails;
import org.egov.collection.integration.models.BillReceiptInfo;
import org.egov.collection.integration.models.PaymentInfo;
import org.egov.collection.integration.models.PaymentInfo.TYPE;
import org.egov.collection.integration.models.PaymentInfoCard;
import org.egov.collection.integration.models.PaymentInfoCash;
import org.egov.collection.integration.models.PaymentInfoChequeDD;
import org.egov.collection.integration.services.CollectionIntegrationService;
import org.egov.commons.Bank;
import org.egov.commons.CChartOfAccounts;
import org.egov.commons.dao.BankHibernateDAO;
import org.egov.dcb.bean.CashPayment;
import org.egov.dcb.bean.ChequePayment;
import org.egov.dcb.bean.CreditCardPayment;
import org.egov.dcb.bean.DDPayment;
import org.egov.dcb.bean.Payment;
import org.egov.demand.dao.EgBillDao;
import org.egov.demand.interfaces.Billable;
import org.egov.demand.model.EgBill;
import org.egov.demand.model.EgBillDetails;
import org.egov.demand.model.EgDemand;
import org.egov.infra.exception.ApplicationRuntimeException;
import org.egov.infra.utils.EgovThreadLocals;
import org.egov.ptis.constants.PropertyTaxConstants;
import org.egov.ptis.domain.dao.demand.PtDemandDao;
import org.egov.ptis.domain.dao.property.BasicPropertyDAO;
import org.egov.ptis.domain.entity.property.BasicProperty;
import org.egov.ptis.domain.entity.property.PropertyOwnerInfo;
import org.egov.ptis.domain.model.ArrearDetails;
import org.egov.ptis.domain.model.AssessmentDetails;
import org.egov.ptis.domain.model.ErrorDetails;
import org.egov.ptis.domain.model.RestPropertyTaxDetails;
import org.egov.ptis.domain.service.property.PropertyExternalService;
import org.egov.wtms.application.entity.WaterConnectionDetails;
import org.egov.wtms.application.service.ConnectionDemandService;
import org.egov.wtms.application.service.WaterConnectionDetailsService;
import org.egov.wtms.masters.entity.PayWaterTaxDetails;
import org.egov.wtms.masters.entity.WaterReceiptDetails;
import org.egov.wtms.masters.entity.WaterTaxDetails;
import org.egov.wtms.masters.entity.enums.ConnectionStatus;
import org.egov.wtms.masters.entity.enums.ConnectionType;
import org.egov.wtms.utils.PropertyExtnUtils;
import org.egov.wtms.utils.WaterTaxNumberGenerator;
import org.egov.wtms.utils.constants.WaterTaxConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

public class WaterTaxExternalService {

    @Autowired
    private PropertyExtnUtils propertyExtnUtils;

    @Autowired
    private WaterConnectionDetailsService waterConnectionDetailsService;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private CollectionIntegrationService collectionService;

    @Autowired
    private ConnectionDemandService connectionDemandService;

    @Autowired
    private WaterTaxNumberGenerator waterTaxNumberGenerator;

    @Autowired
    private ConnectionBillService connectionBillService;

    @Autowired
    private EgBillDao egBillDAO;
    @Autowired
	private BasicPropertyDAO basicPropertyDAO;
    @Autowired
    private PtDemandDao ptDemandDAO;

    @Autowired
    private BankHibernateDAO bankHibernateDAO;
    
    public WaterReceiptDetails payWaterTax(final PayWaterTaxDetails payWaterTaxDetails) {
        WaterReceiptDetails waterReceiptDetails = null;
        ErrorDetails errorDetails = null;
        String currentInstallmentYear = null;
        final SimpleDateFormat formatYear = new SimpleDateFormat("yyyy");
         WaterConnectionDetails waterConnectionDetails =null;
        if(payWaterTaxDetails.getApplicaionNumber() !=null && !"".equals(payWaterTaxDetails.getApplicaionNumber())){
            waterConnectionDetails=waterConnectionDetailsService.findByApplicationNumber(payWaterTaxDetails.getApplicaionNumber());
  
        }else if(payWaterTaxDetails.getConsumerNo() != null){
            waterConnectionDetails=waterConnectionDetailsService.findByApplicationNumberOrConsumerCodeAndStatus(payWaterTaxDetails.getConsumerNo(), ConnectionStatus.ACTIVE);
       }
        final WaterConnectionBillable waterConnectionBillable = (WaterConnectionBillable) context
                .getBean("waterConnectionBillable");
        final AssessmentDetails assessmentDetails = propertyExtnUtils.getAssessmentDetailsForFlag(
                waterConnectionDetails.getConnection().getPropertyIdentifier(),
                PropertyExternalService.FLAG_FULL_DETAILS);
        waterConnectionBillable.setWaterConnectionDetails(waterConnectionDetails);
        waterConnectionBillable.setAssessmentDetails(assessmentDetails);
        waterConnectionBillable.setUserId(Long.valueOf("16"));
        EgovThreadLocals.setUserId(Long.valueOf("16"));
        if (ConnectionStatus.INPROGRESS.equals(waterConnectionDetails.getConnectionStatus()))
            currentInstallmentYear = formatYear.format(connectionDemandService.getCurrentInstallment(
                    WaterTaxConstants.EGMODULE_NAME, WaterTaxConstants.YEARLY, new Date()).getInstallmentYear());
        else if (ConnectionStatus.ACTIVE.equals(waterConnectionDetails.getConnectionStatus())
                && ConnectionType.NON_METERED.equals(waterConnectionDetails.getConnectionType()))
            currentInstallmentYear = formatYear.format(connectionDemandService.getCurrentInstallment(
                    WaterTaxConstants.WATER_RATES_NONMETERED_PTMODULE, null, new Date()).getInstallmentYear());
        else if (ConnectionStatus.ACTIVE.equals(waterConnectionDetails.getConnectionStatus())
                && ConnectionType.METERED.equals(waterConnectionDetails.getConnectionType()))
            currentInstallmentYear = formatYear.format(connectionDemandService.getCurrentInstallment(
                    WaterTaxConstants.EGMODULE_NAME, WaterTaxConstants.MONTHLY, new Date()).getInstallmentYear());
        waterConnectionBillable.setReferenceNumber(waterTaxNumberGenerator.generateBillNumber(currentInstallmentYear));
        waterConnectionBillable.setBillType(connectionDemandService.getBillTypeByCode(BILLTYPE_MANUAL));
        waterConnectionBillable.setTransanctionReferenceNumber(payWaterTaxDetails.getTransactionId());
        final EgBill egBill = generateBill(waterConnectionBillable);

        final BillReceiptInfo billReceiptInfo = getBillReceiptInforForwaterTax(payWaterTaxDetails, egBill);
        if (null != billReceiptInfo) {
            waterReceiptDetails = new WaterReceiptDetails();
            waterReceiptDetails.setReceiptNo(billReceiptInfo.getReceiptNum());
            waterReceiptDetails.setReceiptDate(formatDate(billReceiptInfo.getReceiptDate()));
            waterReceiptDetails.setPayeeName(billReceiptInfo.getPayeeName());
            waterReceiptDetails.setPayeeAddress(billReceiptInfo.getPayeeAddress());
            waterReceiptDetails.setBillReferenceNo(billReceiptInfo.getBillReferenceNum());
            waterReceiptDetails.setServiceName(billReceiptInfo.getServiceName());
            waterReceiptDetails.setDescription(billReceiptInfo.getDescription());
            waterReceiptDetails.setPaidBy(billReceiptInfo.getPaidBy());
            waterReceiptDetails.setPaymentMode(payWaterTaxDetails.getPaymentMode());
            waterReceiptDetails.setPaymentAmount(billReceiptInfo.getTotalAmount());
            //waterReceiptDetails.setTotalAmountPaid(billReceiptInfo.getTotalAmount());
            //waterReceiptDetails.setCollectionType(billReceiptInfo.getCollectionType());
            waterReceiptDetails.setTransactionId(billReceiptInfo.getManualReceiptNumber());
            errorDetails = new ErrorDetails();
            errorDetails.setErrorCode(WaterTaxConstants.THIRD_PARTY_ERR_CODE_SUCCESS);
            errorDetails.setErrorMessage(WaterTaxConstants.THIRD_PARTY_ERR_MSG_SUCCESS);

            waterReceiptDetails.setErrorDetails(errorDetails);
        }
        return waterReceiptDetails;
    }
    
    public WaterTaxDetails getWaterTaxDemandDet(PayWaterTaxDetails payWaterTaxDetails)
    {
        WaterTaxDetails waterTaxDetails=new WaterTaxDetails();
        WaterConnectionDetails waterConnectionDetails =null;
        ErrorDetails errorDetails=null;
        if(payWaterTaxDetails.getApplicaionNumber() !=null && !"".equals(payWaterTaxDetails.getApplicaionNumber())){
            waterConnectionDetails=waterConnectionDetailsService.findByApplicationNumber(payWaterTaxDetails.getApplicaionNumber());
  
        }else if(payWaterTaxDetails.getConsumerNo() != null){
            waterConnectionDetails=waterConnectionDetailsService.findByApplicationNumberOrConsumerCodeAndStatus(payWaterTaxDetails.getConsumerNo(), ConnectionStatus.ACTIVE);
       }
        waterTaxDetails.setConsumerNo(waterConnectionDetails.getConnection().getConsumerCode());
        String propertyIdentifier = waterConnectionDetails.getConnection().getPropertyIdentifier();
        final BasicProperty basicProperty = basicPropertyDAO.getBasicPropertyByPropertyID(propertyIdentifier);
        waterTaxDetails.setPropertyAddress(basicProperty.getAddress().toString());
        waterTaxDetails.setLocalityName(basicProperty.getPropertyID().getLocality().getName());
        
        
        final List<PropertyOwnerInfo> propOwnerInfos =basicProperty.getPropertyOwnerInfo();
        if(propOwnerInfos.size()>0)
        {
        	waterTaxDetails.setOwnerName(propOwnerInfos.get(0).getOwner().getName());
        	waterTaxDetails.setMobileNo(propOwnerInfos.get(0).getOwner().getMobileNumber());
        }
        
       // final List<ArrearDetails> arrearDetailsList = new ArrayList<ArrearDetails>();
        final List<Object> list = ptDemandDAO.getTaxDetailsForWaterConnection(waterConnectionDetails.getConnection().getConsumerCode(),waterConnectionDetails.getConnectionType().name());
        if(list.size()>0)
        {
        	waterTaxDetails.setTaxDetails(new ArrayList<RestPropertyTaxDetails>());
        }
        String loopInstallment="";
        RestPropertyTaxDetails arrearDetails=null;
        BigDecimal total=BigDecimal.ZERO;
        for (final Object record : list) {
        	
            final Object[] data = (Object[]) record;
            final String taxType = (String) data[0];
            
            final String installment=(String)data[1];
            final BigInteger dmd = (BigInteger) data[2];
            final Double col = (Double) data[3];
            BigDecimal demand=BigDecimal.valueOf(dmd.intValue());
            BigDecimal collection=BigDecimal.valueOf(col.doubleValue());
            if(loopInstallment.isEmpty())
            {
        		loopInstallment=installment;
        		arrearDetails = new RestPropertyTaxDetails();
        		arrearDetails.setInstallment(installment);
            }
            if(loopInstallment.equals(installment))
            {
            	
            	if(PropertyTaxConstants.REASON_CATEGORY_CODE_PENALTY.equalsIgnoreCase(taxType))
            	{
            		arrearDetails.setPenalty(demand.subtract(collection));
            	}
            	else if(PropertyTaxConstants.DEMANDRSN_CODE_CHQ_BOUNCE_PENALTY.equalsIgnoreCase(taxType))
            	{
            		arrearDetails.setChqBouncePenalty(demand.subtract(collection));
            	}
            	else 
            	{
            		total = total.add(demand.subtract(collection));
            	}
            	
            	
            	
            }else
            {
            	
            	arrearDetails.setTaxAmount(total);   
            	arrearDetails.setTotalAmount(total.add(arrearDetails.getPenalty()).add(arrearDetails.getChqBouncePenalty()));
            	waterTaxDetails.getTaxDetails().add(arrearDetails);
            	loopInstallment=installment;
        		arrearDetails = new RestPropertyTaxDetails();
        		arrearDetails.setInstallment(installment);
        		total=BigDecimal.ZERO;
            	
            }
            	
           
        }
        if(arrearDetails!=null)
    	{
        arrearDetails.setTaxAmount(total);   
    	arrearDetails.setTotalAmount(total.add(arrearDetails.getPenalty()).add(arrearDetails.getChqBouncePenalty()));
    	waterTaxDetails.getTaxDetails().add(arrearDetails);
    	}    
        errorDetails = new ErrorDetails();
        errorDetails.setErrorCode(WaterTaxConstants.THIRD_PARTY_ERR_CODE_SUCCESS);
        errorDetails.setErrorMessage(WaterTaxConstants.THIRD_PARTY_ERR_MSG_SUCCESS);

        waterTaxDetails.setErrorDetails(errorDetails);
        return waterTaxDetails;
    }

    @Transactional
    public BillReceiptInfo executeCollection(final Payment payment, final EgBill bill) {

        if (!isCollectionPermitted(bill))
            throw new ApplicationRuntimeException(
                    "Collection is not allowed - current balance is zero and advance coll exists.");

        final List<PaymentInfo> paymentInfoList = preparePaymentInfo(payment);

        final BillInfoImpl billInfo = prepareBillInfo(payment.getAmount(), COLLECTIONTYPE.F, bill);
        return collectionService.createReceipt(billInfo, paymentInfoList);
    }

    private List<PaymentInfo> preparePaymentInfo(final Payment payment) {
        final List<PaymentInfo> paymentInfoList = new ArrayList<PaymentInfo>();
        PaymentInfo paymentInfo = null;
        if (payment != null)
            if (payment instanceof ChequePayment) {
                final ChequePayment chequePayment = (ChequePayment) payment;
                paymentInfo = new PaymentInfoChequeDD(chequePayment.getBankId(), chequePayment.getBranchName(),
                        chequePayment.getInstrumentDate(), chequePayment.getInstrumentNumber(), TYPE.cheque,
                        payment.getAmount());

            } else if (payment instanceof DDPayment) {
                final DDPayment chequePayment = (DDPayment) payment;
                paymentInfo = new PaymentInfoChequeDD(chequePayment.getBankId(), chequePayment.getBranchName(),
                        chequePayment.getInstrumentDate(), chequePayment.getInstrumentNumber(), TYPE.dd,
                        payment.getAmount());

            } else if (payment instanceof CreditCardPayment)
                paymentInfo = prepareCardPaymentInfo((CreditCardPayment) payment, new PaymentInfoCard());
            else if (payment instanceof CashPayment)
                paymentInfo = new PaymentInfoCash(payment.getAmount());
        paymentInfoList.add(paymentInfo);
        return paymentInfoList;
    }

    /**
     * Apportions the paid amount amongst the appropriate GL codes and returns
     * the collections object that can be sent to the collections API for
     * processing.
     * 
     * @param bill
     * @param amountPaid
     * @return
     */
    private BillInfoImpl prepareBillInfo(final BigDecimal amountPaid, final COLLECTIONTYPE collType, final EgBill bill) {
        final BillInfoImpl billInfoImpl = initialiseFromBill(amountPaid, collType, bill);

        final ArrayList<ReceiptDetail> receiptDetails = new ArrayList<ReceiptDetail>();
        final List<EgBillDetails> billDetails = new ArrayList<EgBillDetails>(bill.getEgBillDetails());
        Collections.sort(billDetails);

        for (final EgBillDetails billDet : billDetails)
            receiptDetails.add(initReceiptDetail(billDet.getGlcode(), BigDecimal.ZERO, // billDet.getCrAmount(),
                    billDet.getCrAmount(), billDet.getDrAmount(), billDet.getDescription()));

        // new
        // PropertyTaxCollection().apportionPaidAmount(String.valueOf(bill.getId()),
        // amountPaid, receiptDetails);

        for (final EgBillDetails billDet : bill.getEgBillDetails())
            for (final ReceiptDetail rd : receiptDetails)
                // FIX ME
                if (billDet.getGlcode().equals(rd.getAccounthead().getGlcode())
                        && billDet.getDescription().equals(rd.getDescription())) {
                    final BillAccountDetails billAccDetails = new BillAccountDetails(billDet.getGlcode(),
                            billDet.getOrderNo(), rd.getCramountToBePaid(), rd.getDramount(),
                            billDet.getFunctionCode(), billDet.getDescription(), null );
                    billInfoImpl.getPayees().get(0).getBillDetails().get(0).addBillAccountDetails(billAccDetails);
                    break;
                }
        billInfoImpl.setTransactionReferenceNumber(bill.getTransanctionReferenceNumber());
        return billInfoImpl;
    }

    private BillInfoImpl initialiseFromBill(final BigDecimal amountPaid, final COLLECTIONTYPE collType,
            final EgBill bill) {
        BillInfoImpl billInfoImpl = null;
        BillPayeeDetails billPayeeDet = null;
        final List<BillPayeeDetails> billPayeeDetList = new ArrayList<BillPayeeDetails>();
        final List<String> collModesList = new ArrayList<String>();
        final String[] collModes = bill.getCollModesNotAllowed().split(",");
        for (final String coll : collModes)
            collModesList.add(coll);
        billInfoImpl = new BillInfoImpl(bill.getServiceCode(), bill.getFundCode(), bill.getFunctionaryCode(),
                bill.getFundSourceCode(), bill.getDepartmentCode(), "Water Tax collection", bill.getCitizenName(),
                bill.getPartPaymentAllowed(), bill.getOverrideAccountHeadsAllowed(), collModesList, collType);
        billPayeeDet = new BillPayeeDetails(bill.getCitizenName(), bill.getCitizenAddress());

        final BillDetails billDetails = new BillDetails(bill.getId().toString(), bill.getCreateDate(),
                bill.getConsumerId(), bill.getBoundaryNum().toString(), bill.getBoundaryType(), bill.getDescription(),
                amountPaid, // the actual amount paid, which might include
                            // advances
                bill.getMinAmtPayable());
        billPayeeDet.addBillDetails(billDetails);
        billPayeeDetList.add(billPayeeDet);
        billInfoImpl.setPayees(billPayeeDetList);
        return billInfoImpl;
    }

    private ReceiptDetail initReceiptDetail(final String glCode, final BigDecimal crAmount,
            final BigDecimal crAmountToBePaid, final BigDecimal drAmount, final String description) {
        final ReceiptDetail receiptDetail = new ReceiptDetail();
        final CChartOfAccounts accountHead = new CChartOfAccounts();
        accountHead.setGlcode(glCode);
        receiptDetail.setAccounthead(accountHead);
        receiptDetail.setDescription(description);
        receiptDetail.setCramount(crAmount);
        receiptDetail.setCramountToBePaid(crAmountToBePaid);
        receiptDetail.setDramount(drAmount);
        return receiptDetail;
    }

    private PaymentInfoCard prepareCardPaymentInfo(final CreditCardPayment cardPayment,
            final PaymentInfoCard paymentInfoCard) {
        paymentInfoCard.setInstrumentNumber(cardPayment.getCreditCardNo());
        paymentInfoCard.setInstrumentAmount(cardPayment.getAmount());
        paymentInfoCard.setExpMonth(cardPayment.getExpMonth());
        paymentInfoCard.setExpYear(cardPayment.getExpYear());
        paymentInfoCard.setCvvNumber(cardPayment.getCvv());
        paymentInfoCard.setCardTypeValue(cardPayment.getCardType());
        paymentInfoCard.setTransactionNumber(cardPayment.getTransactionNumber());
        return paymentInfoCard;
    }

    private boolean isCollectionPermitted(final EgBill bill) {
        final boolean allowed = thereIsCurrentBalanceToBePaid(bill);

        return allowed;
    }

    private boolean thereIsCurrentBalanceToBePaid(final EgBill bill) {
        boolean result = false;
        BigDecimal currentBal = BigDecimal.ZERO;
        for (final Map.Entry<String, String> entry : WaterTaxConstants.GLCODEMAP_FOR_CURRENTTAX.entrySet())
            currentBal = currentBal.add(bill.balanceForGLCode(entry.getValue()));
        if (currentBal != null && currentBal.compareTo(BigDecimal.ZERO) > 0)
            result = true;
        return result;
    }

    public BillReceiptInfo getBillReceiptInforForwaterTax(final PayWaterTaxDetails payWaterTaxDetails,
            final EgBill egBill) {

   
        final Map<String, String> paymentDetailsMap = new HashMap<String, String>();
        paymentDetailsMap.put(PropertyTaxConstants.TOTAL_AMOUNT, payWaterTaxDetails.getPaymentAmount().toString());
        paymentDetailsMap.put(PropertyTaxConstants.PAID_BY, payWaterTaxDetails.getPaidBy());
        if(PropertyTaxConstants.THIRD_PARTY_PAYMENT_MODE_CHEQUE.equalsIgnoreCase(payWaterTaxDetails.getPaymentMode().toLowerCase()))
        {
        paymentDetailsMap.put(ChequePayment.INSTRUMENTNUMBER, payWaterTaxDetails.getChqddNo());
        paymentDetailsMap.put(ChequePayment.INSTRUMENTDATE, payWaterTaxDetails.getChqddDate());
        paymentDetailsMap.put(ChequePayment.BRANCHNAME, payWaterTaxDetails.getBranchName());
        Long validatesBankId = validateBank(payWaterTaxDetails.getBankName());
        paymentDetailsMap.put(ChequePayment.BANKID, validatesBankId.toString());
        paymentDetailsMap.put(ChequePayment.BANKNAME, payWaterTaxDetails.getBankName());
        }
        final Payment payment = Payment.create(payWaterTaxDetails.getPaymentMode().toLowerCase(), paymentDetailsMap);
      
        final BillReceiptInfo billReceiptInfo = executeCollection(payment, egBill);
        return billReceiptInfo;
    }

    private String formatDate(final Date date) {
        final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        return sdf.format(date);
    }

    
    private Long validateBank(String bankCodeOrName) {
		
   	 Bank bank = bankHibernateDAO.getBankByCode(bankCodeOrName);
        if (bank == null) {
            // Tries by name if code not found
            bank = bankHibernateDAO.getBankByCode(bankCodeOrName);
        }
        return new Long(bank.getId());
   
		
	}
    
    public BillReceiptInfo validateTransanctionIdPresent(final String transantion) {
       return (BillReceiptInfo) collectionService.getReceiptInfo(WaterTaxConstants.COLLECTION_STRING_SERVICE_CODE, transantion);
    }
    // TODO:EgBillDao is not intialising Request is comes from RestController so
    // using BillCreation method here only
    public final EgBill generateBill(final Billable billObj) {
        final EgBill bill = new EgBill();
        bill.setBillNo(billObj.getReferenceNumber());
        bill.setBoundaryNum(billObj.getBoundaryNum().intValue());
        bill.setTransanctionReferenceNumber(billObj.getTransanctionReferenceNumber());
        bill.setBoundaryType(billObj.getBoundaryType());
        bill.setCitizenAddress(billObj.getBillAddress());
        bill.setCitizenName(billObj.getBillPayee());
        bill.setCollModesNotAllowed(billObj.getCollModesNotAllowed());
        bill.setDepartmentCode(billObj.getDepartmentCode());
        bill.setEgBillType(billObj.getBillType());
        bill.setFunctionaryCode(billObj.getFunctionaryCode());
        bill.setFundCode(billObj.getFundCode());
        bill.setFundSourceCode(billObj.getFundSourceCode());
        bill.setIssueDate(new Date());
        bill.setLastDate(billObj.getBillLastDueDate());
        bill.setModule(billObj.getModule());
        bill.setOverrideAccountHeadsAllowed(billObj.getOverrideAccountHeadsAllowed());
        bill.setPartPaymentAllowed(billObj.getPartPaymentAllowed());
        bill.setServiceCode(billObj.getServiceCode());
        bill.setIs_Cancelled("N");
        bill.setIs_History("N");
        bill.setModifiedDate(new Date());
        bill.setTotalAmount(billObj.getTotalAmount());
        bill.setUserId(billObj.getUserId());
        bill.setCreateDate(new Date());
        final EgDemand currentDemand = billObj.getCurrentDemand();
        bill.setEgDemand(currentDemand);
        bill.setDescription(billObj.getDescription());
        bill.setDisplayMessage(billObj.getDisplayMessage());

        if (currentDemand != null && currentDemand.getMinAmtPayable() != null)
            bill.setMinAmtPayable(currentDemand.getMinAmtPayable());
        else
            bill.setMinAmtPayable(BigDecimal.ZERO);

        // Get it from the concrete implementation
        final List<EgBillDetails> bd = connectionBillService.getBilldetails(billObj);
        for (final EgBillDetails billdetails : bd) {
            bill.addEgBillDetails(billdetails);
            billdetails.setEgBill(bill);
        }

        bill.setConsumerId(billObj.getConsumerId());
        bill.setCallBackForApportion(billObj.isCallbackForApportion());
        egBillDAO.create(bill);
        return bill;
    };
}