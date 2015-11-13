<!-- #-------------------------------------------------------------------------------
# eGov suite of products aim to improve the internal efficiency,transparency, 
#    accountability and the service delivery of the government  organizations.
# 
#     Copyright (C) <2015>  eGovernments Foundation
# 
#     The updated version of eGov suite of products as by eGovernments Foundation 
#     is available at http://www.egovernments.org
# 
#     This program is free software: you can redistribute it and/or modify
#     it under the terms of the GNU General Public License as published by
#     the Free Software Foundation, either version 3 of the License, or
#     any later version.
# 
#     This program is distributed in the hope that it will be useful,
#     but WITHOUT ANY WARRANTY; without even the implied warranty of
#     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#     GNU General Public License for more details.
# 
#     You should have received a copy of the GNU General Public License
#     along with this program. If not, see http://www.gnu.org/licenses/ or 
#     http://www.gnu.org/licenses/gpl.html .
# 
#     In addition to the terms of the GPL license to be adhered to in using this
#     program, the following additional terms are to be complied with:
# 
# 	1) All versions of this program, verbatim or modified must carry this 
# 	   Legal Notice.
# 
# 	2) Any misrepresentation of the origin of the material is prohibited. It 
# 	   is required that all modified versions of this material be marked in 
# 	   reasonable ways as different from the original version.
# 
# 	3) This license does not grant any rights to any user of the program 
# 	   with regards to rights under trademark law for use of the trade names 
# 	   or trademarks of eGovernments Foundation.
# 
#   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
#------------------------------------------------------------------------------- -->
<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/includes/taglibs.jsp"%>
<%@ page import="org.egov.ptis.constants.PropertyTaxConstants"%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<html>
<head>
<title><s:text name='transferProperty' /></title>
<script type="text/javascript">
	jQuery.noConflict();
	jQuery("#loadingMask").remove();
	function generateMutationCertificate(actionName) {
		window.location = "printNotice.action?mutationId=" + mutationId.value + "&actionType=" + actionName;
	}

	function onSubmit() {
		var actionName = document.getElementById("workFlowAction").value;
		if (actionName == 'Forward') {
			document.forms[0].action = '/ptis/property/transfer/forward.action';
		} else if (actionName == 'Reject') {
			document.forms[0].action = '/ptis/property/transfer/reject.action';
		} else if (actionName == 'Approve') {
			document.forms[0].action = '/ptis/property/transfer/approve.action';
		} else {
			generateMutationCertificate(actionName);
			return false;
		}
		document.forms[0].submit;
		return true;
	}
</script>
<script
	src="<c:url value='/resources/global/js/egov/inbox.js' context='/egi'/>"></script>
</head>
<div class="formmainbox">
	<s:if test="%{hasErrors()}">
		<div class="errorstyle" id="property_error_area">
			<s:actionerror />
		</div>
	</s:if>

	<s:form action="" name="transferform" theme="simple">
		<s:push value="model">
			<s:hidden name="mode" id="mode" value="%{mode}"></s:hidden>
			<s:hidden name="mutationId" id="mutationId" value="%{mutationId}"></s:hidden>
			<s:hidden name="basicPropId" id="basicPropId"
				value="%{basicproperty.id}"></s:hidden>
			<div class="headingbg">
				<s:text name="transferProperty" />
			</div>
			<table width="100%" border="0" cellspacing="0" cellpadding="0">
				<table width="100%" border="0" cellspacing="0" cellpadding="0">
					<tr>
						<td class="bluebox2" style="width: 5%;">&nbsp;</td>
						<td class="bluebox" style="width: 20%"><s:text name="prop.Id"></s:text>
							:</td>
						<td class="bluebox"><span class="bold"><s:property
									value="basicproperty.upicNo" default="N/A" /></span> <s:hidden
								name="assessmentNo" value="%{basicproperty.upicNo}" /></td>
						<td class="bluebox">&nbsp;</td>
						<td style="width: 25%;">&nbsp;</td>
					</tr>
					<tr>
						<td class="bluebox2">&nbsp;</td>
						<td class="bluebox"><s:text name="PropertyAddress"></s:text>
							:</td>
						<td class="bluebox"><span class="bold"><s:property
									value="basicproperty.address" default="N/A" /></span></td>
						<td class="bluebox"><s:text name="Zone"></s:text> :</td>
						<td class="bluebox"><span class="bold"><s:property
									value="basicproperty.propertyID.zone.name" default="N/A" /></span></td>
					</tr>

					<tr>
						<td class="greybox2">&nbsp;</td>
						<td class="greybox"><s:text name="Ward" /> :</td>
						<td class="greybox"><span class="bold"><s:property
									value="basicproperty.propertyID.ward.name" default="N/A" /></span></td>
						<td class="greybox"><s:text name="block" /> :</td>
						<td class="greybox"><span class="bold"><s:property
									value="basicproperty.propertyID.area.name" default="N/A" /></span></td>
					</tr>

					<tr>
						<td class="greybox2">&nbsp;</td>
						<td class="greybox"><s:text name="currentpropertytax" /> :</td>
						<td class="greybox"><span class="bold">Rs. <s:property
									value="currentPropertyTax" /> /-
						</span></td>
					</tr>
					<tr>
						<td colspan="5">
							<div class="headingsmallbg">
								<span class="bold"><s:text name="ownerdetails.title"></s:text></span>
							</div>
						</td>
					</tr>
					<tr>
						<td colspan="5">
							<table class="tablebottom" id="" width="100%" border="0"
								cellpadding="0" cellspacing="0">
								<tbody>
									<tr>
										<th class="bluebgheadtd"><s:text name="adharno" /></th>
										<th class="bluebgheadtd"><s:text name="MobileNumber" /></th>
										<th class="bluebgheadtd"><s:text name="OwnerName" /></th>
										<th class="bluebgheadtd"><s:text name="gender" /></th>
										<th class="bluebgheadtd"><s:text name="EmailAddress" /></th>
										<th class="bluebgheadtd"><s:text name="GuardianRelation" /></th>
										<th class="bluebgheadtd"><s:text name="Guardian" /></th>
									</tr>
									<s:iterator value="basicproperty.propertyOwnerInfo"
										status="status">
										<tr>
											<td class="blueborderfortd" align="center"><span
												class="bold"> <s:if
														test='%{owner.aadhaarNumber == ""}'>
								        				N/A
								        			</s:if> <s:else>
														<s:property value="%{owner.aadhaarNumber}" default="N/A" />
													</s:else>
											</span></td>
											<td class="blueborderfortd" align="center"><span
												class="bold"><s:property value="owner.mobileNumber" /></span></td>
											<td class="blueborderfortd" align="center"><span
												class="bold"><s:property value="owner.name" /></span></td>
											<td class="blueborderfortd" align="center"><span
												class="bold"><s:property value="owner.gender" /></span></td>
											<td class="blueborderfortd" align="center"><span
												class="bold"> <s:if test='%{owner.emailId == ""}'>N/A</s:if>
													<s:else>
														<s:property value="%{owner.emailId}" />
													</s:else>
											</span></td>
											<td class="blueborderfortd" align="center"><span
												class="bold"><s:property
														value="owner.guardianRelation" default="N/A" /></span></td>
											<td class="blueborderfortd" align="center"><span
												class="bold"><s:property value="owner.guardian"
														default="N/A" /></span></td>
										</tr>
									</s:iterator>
								</tbody>
							</table>
						</td>
					</tr>
					<s:if
						test="%{!@org.egov.ptis.constants.PropertyTaxConstants@WF_STATE_COMMISSIONER_APPROVED.equalsIgnoreCase(state.value)}">
						<tr>
							<td colspan="5">
								<div class="headingsmallbg">
									<s:text name="transferDtls" />
								</div>
							</td>
						</tr>
						<tr>
							<td colspan="5">
								<table width="100%" border="0" cellspacing="0" cellpadding="0"
									class="tablebottom" id="nameTable">
									<tr>
										<th class="bluebgheadtd"><s:text name="adharno" /></th>
										<th class="bluebgheadtd"><s:text name="MobileNumber" />(without
											+91)</th>
										<th class="bluebgheadtd"><s:text name="OwnerName" /></th>
										<th class="bluebgheadtd"><s:text name="gender" /></th>
										<th class="bluebgheadtd"><s:text name="EmailAddress" /></th>
										<th class="bluebgheadtd"><s:text name="GuardianRelation" /></th>
										<th class="bluebgheadtd"><s:text name="Guardian" /></th>
									</tr>
									<s:iterator value="transfereeInfos" status="ownerStatus">
										<tr>
											<td class="blueborderfortd" align="center"><span
												class="bold"> <s:if
														test='%{transfereeInfos[#ownerStatus.index].aadhaarNumber == ""}'>N/A</s:if>
													<s:else>
														<s:property
															value="%{transfereeInfos[#ownerStatus.index].aadhaarNumber}" />
													</s:else>
											</span></td>
											<td class="blueborderfortd" align="center"><span
												class="bold"><s:property
														value="%{transfereeInfos[#ownerStatus.index].mobileNumber}" /></span>
											</td>
											<td class="blueborderfortd" align="center"><span
												class="bold"><s:property
														value="%{transfereeInfos[#ownerStatus.index].name}" /></span></td>
											<td class="blueborderfortd" align="center"><span
												class="bold"><s:property
														value="%{transfereeInfos[#ownerStatus.index].gender}" /></span></td>
											<td class="blueborderfortd" align="center"><span
												class="bold"> <s:if
														test='%{transfereeInfos[#ownerStatus.index].emailId == ""}'>N/A</s:if>
													<s:else>
														<s:property
															value="%{transfereeInfos[#ownerStatus.index].emailId}" />
													</s:else>
											</span></td>
											<td class="blueborderfortd" align="center"><span
												class="bold"><s:property
														value="%{transfereeInfos[#ownerStatus.index].guardianRelation}" /></span>
											</td>
											<td class="blueborderfortd" align="center"><span
												class="bold"><s:property
														value="%{transfereeInfos[#ownerStatus.index].guardian}" /></span></td>
										</tr>
									</s:iterator>
								</table>
							</td>
						</tr>
					</s:if>
					<s:else>
						<tr>
							<td colspan="5">
								<div class="headingsmallbg">
									<s:text name="transferorDetails" />
								</div>
							</td>
						</tr>
						<tr>
							<td colspan="5">
								<table width="100%" border="0" cellspacing="0" cellpadding="0"
									class="tablebottom" id="nameTable">
									<tr>
										<th class="bluebgheadtd"><s:text name="adharno" /></th>
										<th class="bluebgheadtd"><s:text name="MobileNumber" />(without
											+91)</th>
										<th class="bluebgheadtd"><s:text name="OwnerName" /></th>
										<th class="bluebgheadtd"><s:text name="gender" /></th>
										<th class="bluebgheadtd"><s:text name="EmailAddress" /></th>
										<th class="bluebgheadtd"><s:text name="GuardianRelation" /></th>
										<th class="bluebgheadtd"><s:text name="Guardian" /></th>
									</tr>
									<s:iterator value="transfereeInfos" status="ownerStatus">
										<tr>
											<td class="blueborderfortd" align="center"><span
												class="bold"> <s:if
														test='%{transferorInfos[#ownerStatus.index].aadhaarNumber == ""}'>N/A</s:if>
													<s:else>
														<s:property
															value="%{transferorInfos[#ownerStatus.index].aadhaarNumber}" />
													</s:else>
											</span></td>
											<td class="blueborderfortd" align="center"><span
												class="bold"><s:property
														value="%{transferorInfos[#ownerStatus.index].mobileNumber}" /></span>
											</td>
											<td class="blueborderfortd" align="center"><span
												class="bold"><s:property
														value="%{transferorInfos[#ownerStatus.index].name}" /></span></td>
											<td class="blueborderfortd" align="center"><span
												class="bold"><s:property
														value="%{transferorInfos[#ownerStatus.index].gender}" /></span></td>
											<td class="blueborderfortd" align="center"><span
												class="bold"> <s:if
														test='%{transferorInfos[#ownerStatus.index].emailId == ""}'>N/A</s:if>
													<s:else>
														<s:property
															value="%{transferorInfos[#ownerStatus.index].emailId}" />
													</s:else>
											</span></td>
											<td class="blueborderfortd" align="center"><span
												class="bold"><s:property
														value="%{transferorInfos[#ownerStatus.index].guardianRelation}" /></span>
											</td>
											<td class="blueborderfortd" align="center"><span
												class="bold"><s:property
														value="%{transferorInfos[#ownerStatus.index].guardian}" /></span></td>
										</tr>
									</s:iterator>
								</table>
							</td>
						</tr>
					</s:else>
					<tr>
						<td>&nbsp;</td>
					</tr>
					<tr>
						<td class="greybox2">&nbsp;</td>
						<td class="greybox"><s:text name="transferreason"></s:text> :</td>
						<td class="greybox"><span class="bold"><s:property
									value="%{mutationReason.mutationName}" /></span></td>
						<td class="greybox"><s:text name="saleDetls" /> :</td>
						<td class="greybox"><span class="bold"> <s:if
									test="%{saleDetail == ''}">N/A</s:if> <s:else>
									<s:property value="%{saleDetail}" />
								</s:else>
						</span></td>
					</tr>
					<tr>
						<td class="greybox2">&nbsp;</td>
						<td class="greybox"><s:text name="docNum" />:</td>
						<td class="greybox"><span class="bold"><s:property
									value="%{deedNo}" /></span></td>
						<td class="greybox"><s:text name="docDate" />:</td>
						<td class="greybox"><s:date name="deedDate" var="docDate"
								format="dd/MM/yyyy" /> <span class="bold"><s:property
									value="%{#docDate}" /></span></td>
					</tr>
					<s:if
						test="%{!@org.egov.ptis.constants.PropertyTaxConstants@BILL_COLLECTOR_DESGN.equalsIgnoreCase(userDesignation)}">
						<tr>
							<td class="bluebox2">&nbsp;</td>
							<td class="bluebox"><s:text name="docValue" /> :</td>
							<td class="bluebox"><span class="bold"><s:property
										value="%{marketValue}" default="N/A" /></span></td>
							<td class="bluebox"><s:text name="payablefee" />:</td>
							<td class="bluebox"><span class="bold"><s:property
										value="%{mutationFee}" default="N/A" /></span></td>
						</tr>
					</s:if>
					<s:if test="%{!documentTypes.isEmpty()}">
						<tr>
							<%@ include file="../common/DocumentUploadView.jsp"%>
						</tr>
					</s:if>
					<s:if test="%{state != null}">
						<tr>
							<%@ include file="../common/workflowHistoryView.jsp"%>
						<tr>
					</s:if>
				</table>
				<s:if
					test="%{!model.state.nextAction.equalsIgnoreCase(@org.egov.ptis.constants.PropertyTaxConstants@WFLOW_ACTION_READY_FOR_PAYMENT) && 
				!model.state.value.equalsIgnoreCase(@org.egov.ptis.constants.PropertyTaxConstants@TRANSFER_FEE_COLLECTED) && 
				!model.state.value.equalsIgnoreCase(@org.egov.ptis.constants.PropertyTaxConstants@WF_STATE_COMMISSIONER_APPROVED)}">
					<div>
						<%@ include file="../workflow/commonWorkflowMatrix.jsp"%>
					</div>
					<div>
						<%@ include file="../workflow/commonWorkflowMatrix-button.jsp"%>
					</div>
				</s:if>
				<s:elseif
					test="%{model.state.nextAction.equalsIgnoreCase(@org.egov.ptis.constants.PropertyTaxConstants@WF_STATE_COMMISSIONER_APPROVAL_PENDING) ||
					model.state.nextAction.equalsIgnoreCase(@org.egov.ptis.constants.PropertyTaxConstants@WF_STATE_DIGITAL_SIGNATURE_PENDING)}">
					<div id="workflowCommentsDiv" align="center">
						<table width="100%">
							<tr>
								<td width="25%" class="${approverEvenCSS}">&nbsp;</td>
								<td class="${approverEvenCSS}" width="13%">Approver
									Remarks:</td>
								<td class="${approverEvenTextCSS}"><textarea
										id="approverComments" name="approverComments" rows="2"
										value="#approverComments" cols="35"></textarea></td>
								<td class="${approverEvenCSS}">&nbsp;</td>
								<td width="10%" class="${approverEvenCSS}">&nbsp;</td>
								<td class="${approverEvenCSS}">&nbsp;</td>
							</tr>
						</table>
					</div>
					<tr>
						<%@ include file="../workflow/commonWorkflowMatrix-button.jsp"%>
					</tr>
				</s:elseif>
			</table>
		</s:push>
	</s:form>
</div>
</html>