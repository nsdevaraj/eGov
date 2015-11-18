<!--
	eGov suite of products aim to improve the internal efficiency,transparency, 
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
-->
<%@ include file="/includes/taglibs.jsp"%>
<%@ page language="java" pageEncoding="UTF-8"%>
<html>
	<head>
		<title><s:text name="ptis.notice.title" /></title>
		<script type="text/javascript">

	 	jQuery(document).ready( function() {

			var fileStoreIds = '<s:property value="%{fileStoreIds}" />';

			jQuery('<form>.').attr({
				method: 'post',
				action: '/ptis/reports/selectCertificate.jsp',
				target: '_self'
			})
			.append(jQuery('<input>').attr({
			    type: 'hidden',
			    id: 'fileStoreIds',
			    name: 'fileStoreIds',
			    value: fileStoreIds
			})).appendTo( document.body )
			.append(jQuery('<input>').attr({
			    type: 'hidden',
			    id: 'moduleName',
			    name: 'moduleName',
			    value: '<s:property value="%{@org.egov.ptis.constants.PropertyTaxConstants@FILESTORE_MODULE_NAME}"/>'
			})).appendTo( document.body )
			.append(jQuery('<input>').attr({
			    type: 'hidden',
			    id: 'ulbCode',
			    name: 'ulbCode',
			    value: '<s:proeprty value="%{ulbCode}"/>'
			})).appendTo( document.body )
			.append(jQuery('<input>').attr({
			    type: 'hidden',
			    id: 'callBackUrl',
			    name: 'callBackUrl',
			    value: '/ptis/digitalSignature/propertyTax/transitionWorkflow'
			})).appendTo( document.body )
			.submit();
		});
		
		</script>
	</head>
	<body>
	</body>
</html>
