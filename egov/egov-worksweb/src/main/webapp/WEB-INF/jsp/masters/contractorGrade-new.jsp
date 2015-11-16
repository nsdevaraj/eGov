<!-- -------------------------------------------------------------------------------
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
<%@ include file="/includes/taglibs.jsp" %> 
<html>  
	<head>  
    	<title><s:text name="contractor.grade.master.title" /></title>
    	<script src="<egov:url path='resources/js/works.js'/>"></script> 
	</head>  
	<body onload="roundOffMaxMinAmount();">
    	<s:if test="%{hasErrors()}">
        	<div class="errorstyle">
          		<s:actionerror/>
          		<s:fielderror/>
        	</div>
    	</s:if>
		<s:if test="%{hasActionMessages()}">
        	<div class="messagestyle">
        		<s:actionmessage theme="simple"/>
        	</div>
    	</s:if>
   
		<s:form action="contractorGrade-save" theme="simple" name="contractorGrade" id="contractorGrade">
			<s:token/>
			<s:hidden name="model.id" />
			<p>
				<%@ include file='contractorGrade-form.jsp'%>
				<s:if test="%{mode!='view'}">
					<s:submit value="SAVE" method="save" cssClass="buttonfinal" id="saveButton" name="button"/>
				</s:if>
				<s:if test="%{model.id==null}" >
					<input type="button" value="CLEAR" class="buttonfinal" onclick="clearForm(this.form.id)"/>
				</s:if>
				<input type="button" class="buttonfinal" value="CLOSE" id="closeButton" name="closeButton" onclick="window.close();" />
			</p>
		</s:form> 
	</body>
</html>
