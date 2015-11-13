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
package org.egov.wtms.utils.constants;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class WaterTaxConstants {
    public static final String FILESTORE_MODULECODE = "WTMS";
    public static final String MODULE_NAME = "Water Tax Management";
    public static final String PROPERTY_MODULE_NAME = "Property Tax";
    public static final String MODULETYPE = "WATERTAXAPPLICATION";
    public static final String DASH_DELIM = "-";
    public static final String APPROVED = "APPROVED";
    public static final String CREATEWORKFLOWSTATE = "CREATED";
    public static final String APPLICATION_NUMBER = "applicationNumber";
    public static final String NEWCONNECTION = "NEWCONNECTION";
    public static final String METERED = "Metered";
    public static final String CITIZENROLE ="Citizen";
    public static final String CONNECTIONTYPE_METERED = "METERED";
    public static final String NON_METERED = "Non-metered";
    public static final String EGMODULES_NAME = "Water Tax";
    public static final String EGMODULE_NAME = "Water Tax Management";
    public static final String WATERTAX_SECURITY_CHARGE = "WTAXSECURITY";
    public static final String WATERTAX_DONATION_CHARGE = "WTAXDONATION";
    public static final String CONNECTION_FEE = "Connection fee";
    public static final String ADDNLCONNECTION = "ADDNLCONNECTION";
    public static final String CLOSINGCONNECTION = "CLOSINGCONNECTION";
    public static final String RECONNECTIONCONNECTION = "RECONNECTION";
    public static final String CHANGEOFUSE = "CHANGEOFUSE";
    public static final String CLOSURECONN = "Closure Of Connection";
    public static final String SMSEMAILTYPEADDITONALCONNCREATE = "additionalconncreate";
    public static final String SMSEMAILTYPEADDITONALCONNAPPROVE = "additionalconnapprove";
    public static final String SMSEMAILTYPENEWCONNAPPROVE = "newconnapprove";
    public static final String SMSEMAILTYPENEWCONNCREATE = "newconncreate";
    public static final String SMSEMAILTYPENEWCONNEXECUTION = "newconnexecutiondate";
    public static final String SMSEMAILTYPENEWCONNESTNOTICE = "newconnestnotice";
    public static final String SMSEMAILTYPEADDCONNESTNOTICE = "addconnestnotice";
    public static final String DONATIONMASTER = "DONATIONMASTER";

    public static final String SMSEMAILTYPENEWCONNFEEPAID = "newconnfeepaid";
    public static final String SMSEMAILTYPEADDCONNFEEPAID = "addconnfeepaid";

    public static final String SMSEMAILTYPECHANGEOFUSECREATE = "changeofusecreate";
    public static final String SMSEMAILTYPECHANGEOFUSEAPPROVE = "changeofuseapprove";
    public static final String SMSEMAILTYPECHANGEOFUSENOTICE = "changeofusenotice";
    public static final String SMSEMAILTYPECHANGEOFUSEEXECUTION = "changeofuseexecution";
    public static final String SMSEMAILTYPECHANGEOFUSEREJECTION = "changeofuserejection";
    public static final String SMSEMAILTYPECHANGEOFUSEFEEPAID = "changeofusefeepaid";
    public static final String SMSEMAILTYPECLOSINGCONNAPPROVE = "closureofconnectionapprove";
    public static final String SMSEMAILTYPECLOSINGCONNSANCTIONED = "closureofconnectionsanctioned";
    public static final String SMSEMAILTYPERECONNECTIONAPPROVE = "reconnectionofconnectionapprove";
    public static final String SMSEMAILTYPERECONNECTIONSANCTIONED = "reconnectionofconnectionsanctioned";

    public static final String CATEGORY_BPL = "BPL";
    public static final String WATERTAX_FIELDINSPECTION_CHARGE = "WTAXFIELDINSPEC";
    // User Roles
    public static final String CSCOPERTAORROLE = "CSC Operator";
    public static final String CLERKULB = "ULB Operator";
    public static final String CLERKROLEFORADONI = "Senior Assistant,Junior Assistant";
    public static final String APPROVERROLE = "Water Tax Approver";
    public static final String COMMISSIONERDEPARTEMNT = "Administration";
    public static final String SUPERUSER = "Super User";

    // Rest API constants
    public static final String CURR_DMD_STR = "CURR_DMD";
    public static final String ARR_DMD_STR = "ARR_DMD";
    public static final String CURR_COLL_STR = "CURR_COLL";
    public static final String ARR_COLL_STR = "ARR_COLL";

    public static final String CONSUMERCODE_NOT_EXIST_ERR_CODE = "WTAX100";
    public static final String WTAXDETAILS_PROPERTYID_NOT_EXIST_ERR_MSG_PREFIX = "Water Connection details with Assessment Number ";
    public static final String PROPERTYID_NOT_EXIST_ERR_CODE = "WTAX101";
    public static final String WTAXDETAILS_CONSUMER_CODE_NOT_EXIST_ERR_MSG_PREFIX = "Water Connection details with Consumer code ";
    public static final String WTAXDETAILS_NOT_EXIST_ERR_MSG_SUFFIX = " does not exist";

    public static final String BILLTYPE_AUTO = "AUTO";
    public static final String BILLTYPE_MANUAL = "MANUAL";
    public static final String CITIZENUSER = "9999999999";
    public static final String BILLTYPE_ONLINE = "ONLINE";
    public static final String PERMENENTCLOSE = "Permanent";
    public static final String TEMPERARYCLOSE = "Temporary";
    public static final String PERMENENTCLOSECODE = "P";
    public static final String TEMPERARYCLOSECODE = "T";

    public static final String WF_STATE_REJECTED = "Rejected";
    public static final String WFLOW_ACTION_STEP_REJECT = "Reject";
    public static final String WF_STATE_REVENUE_CLERK_APPROVAL_PENDING = "Revenenu Clerk Approval Pending";
    public static final String WF_STATE_TAP_EXECUTION_DATE = "Tap Execution Date";
    public static final String WF_STATE_CLERK_APPROVED = "Clerk approved";
    public static final String WF_STATE_PAYMENT_DONE_AGT_ESTIMATION = "Payment done against Estimation";
    public static final String WF_STATE_COMMISSIONER_APPROVED = "Commissioner Approved";
    public static final String WF_STATE_ASSISTANT_ENGINEER_APPROVED = "Asst engg approved";
    public static final String WF_STATE_WORK_ORDER_GENERETED = "Work order generated";

    public static final String APPROVEWORKFLOWACTION = "Approve";
    public static final String SUBMITWORKFLOWACTION = "Submit";

    public static final String BPL_CATEGORY = "BPL";
    public static final String HIERARCHYNAME_ADMIN = "ADMINISTRATION";
    
    public static final String WF_STATE_BUTTON_GENERATEESTIMATE = "Generate Estimation Notice";
    public static final String WF_STATE_TAP_EXECUTION_DATE_BUTTON = "Tap Execution Date";

    // Application status
    public static final String APPLICATION_STATUS_CREATED = "CREATED";
    public static final String APPLICATION_STATUS_APPROVED = "APPROVED";
    public static final String APPLICATION_STATUS_VERIFIED = "VERIFIED";
    public static final String APPLICATION_STATUS_ESTIMATENOTICEGEN = "ESTIMATIONNOTICEGENERATED";
    public static final String APPLICATION_STATUS_FEEPAID = "ESTIMATIONAMOUNTPAID";
    public static final String APPLICATION_STATUS_WOGENERATED = "WORKORDERGENERATED";
    public static final String APPLICATION_STATUS_SANCTIONED = "SANCTIONED";
    public static final String APPLICATION_STATUS_CANCELLED = "CANCELLED";

    public static final String APPLICATION_STATUS_CLOSERINITIATED = "CLOSERINITIATED";
    public static final String APPLICATION_STATUS_CLOSERINPROGRESS = "CLOSERINPROGRESS";
    public static final String APPLICATION_STATUS_CLOSERAPRROVED = "CLOSERAPPROVED";
    public static final String APPLICATION_STATUS_CLOSERSANCTIONED = "CLOSERSANCTIONED";
    public static final String WORKFLOW_CLOSUREADDITIONALRULE = "CLOSECONNECTION";
    public static final String WORKFLOW_RECONNCTIONINITIATED = "RECONNECTIONINITIATED";
    public static final String APPLICATION_STATUS__RECONNCTIONINPROGRESS = "RECONNECTIONINPROGRESS";
    public static final String APPLICATION_STATUS__RECONNCTIONAPPROVED = "RECONNECTIONAPPROVED";
    public static final String APPLICATION_STATUS__RECONNCTIONSANCTIONED = "RECONNECTIONSANCTIONED";
    public static final String APPLICATION_STATUS_RECONNDIGSIGNUPDATED = "RECONNDIGSIGNUPDATED";
    public static final String APPLICATION_STATUS_CLOSERDIGSIGNUPDATED = "CLOSUREDIGSIGNUPDATED";
    public static final String APPLICATION_STATUS_DIGITALSIGNUPDATED = "DIGITALSIGNATUREUPDATED";
    public static final String SEARCH_MENUTREE_APPLICATIONTYPE_CLOSURE = "CLOSURECONNECTION";
    public static final String SEARCH_MENUTREE_APPLICATIONTYPE_METERED = "METERENTRY";
    public static final String SEARCH_MENUTREE_APPLICATIONTYPE_COLLECTTAX = "COLLECTTAX";

    // appconfig key
    public static final String SENDSMSFORWATERTAX = "SENDSMSFORWATERTAX";
    public static final String WATERTAXWORKFLOWDEPARTEMENT = "DEPARTMENTFORWORKFLOW";
    public static final String CLERKDESIGNATIONFORCSCOPERATOR = "CLERKDESIGNATIONFORCSCOPERATOR";
    public static final String SENDEMAILFORWATERTAX = "SENDEMAILFORWATERTAX";
    public static final String NEWCONNECTIONALLOWEDIFPTDUE = "NEWCONNECTIONALLOWEDIFPTDUE";
    public static final String MULTIPLENEWCONNECTIONFORPID = "MULTIPLENEWCONNECTIONFORPID";
    public static final String DOCUMENTREQUIREDFORBPL = "DOCUMENTREQUIREDFORBPL";
    public static final String ROLEFORNONEMPLOYEEINWATERTAX = "ROLEFORNONEMPLOYEEINWATERTAX";
    public static final String ROLESFORLOGGEDINUSER = "RolesForSearchWAterTaxConnection";

    // this is just another name to new connection
    public static final String PRIMARYCONNECTION = "Primary Connection";
    public static final String CONN_NAME_ADDNLCONNECTION = "Additional Connection";

    public static final String NEW_CONNECTION_MATRIX_ADDL_RULE = "NEWCONNECTION";
    public static final String FEE_COLLECTION_COMMENT = "Water connection fee collected";

    public static final String ADDRULE_FOR_NEW_CONNECTION = "NEWCONNECTION";
    public static final String ADDRULE_FOR_ADD_CONNECTION = "ADDNLCONNECTION";
    public static final String ADDRULE_FOR_CHANGE_OF_USE = "CHANGEOFUSE";
    public static final String WF_WORKORDER_BUTTON = "Generate WorkOrder";
    public static final String YEARLY = "Yearly";
    public static final String MONTHLY = "Monthly";

    public static final String WATERTAXREASONCODE = "WTAXCHARGES";
    public static final String WF_ESTIMATION_NOTICE_BUTTON = "Generate Estimation Notice";
    public static final String WF_CLOSERACKNOWLDGEENT_BUTTON = "Generate Acknowledgement";
    public static final String WF_RECONNECTIONACKNOWLDGEENT_BUTTON = "Generate Reconnection Ack";
    public static final String WATER_RATES_NONMETERED_PTMODULE = "Property Tax";

    public static final String CURR_DUE = "CurrentDue";
    public static final String ARR_DUE = "ArrearDue";
    public static final String ARR_INSTALFROM_STR = "ARR_INSTALL_FROM";

    public static final String WATER_CONN_BILLNO_SEQ = "SEQ_BILLNO_";

    public static final String RESIDENTIAL = "RESIDENTIAL";
    public static final String WFLOW_ACTION_STEP_THIRDPARTY_CREATED = "Created";
    public static final String NONMETEREDDEMANDREASON = "Water tax charges";
    public static final String MASTERSTATUSACTIVE = "ACTIVE";
    public static final String MASTERSTATUSINACTIVE = "IN ACTIVE";

    // HashMap map b/n Demand reason string and code
    public static final LinkedHashMap<String, String> NON_METERED_DMDRSN_CODE_MAP = new LinkedHashMap<String, String>() {
        /**
         *
         */
        private static final long serialVersionUID = -9153822216362973956L;

        {
            put(WATERTAXREASONCODE, NONMETEREDDEMANDREASON);

        }
    };
    public static final String TOTAL_AMOUNT = "amount";
    public final static String PAID_BY = "paidBy";
    public static final HashMap<String, String> GLCODEMAP_FOR_CURRENTTAX = new HashMap<String, String>() {
        /**
         *
         */
        private static final long serialVersionUID = 540382999962934138L;

        {

            put(WATERTAX_DONATION_CHARGE, "1100201");
            put(WATERTAX_SECURITY_CHARGE, "1100201");
            put(WATERTAX_FIELDINSPECTION_CHARGE, "1100201");
            put(WATERTAXREASONCODE, "1405016");

        }
    };
    public static final String THIRD_PARTY_ERR_CODE_SUCCESS = "WTMS-REST-0";
    public static final String THIRD_PARTY_ERR_MSG_SUCCESS = "SUCCESS";
}
