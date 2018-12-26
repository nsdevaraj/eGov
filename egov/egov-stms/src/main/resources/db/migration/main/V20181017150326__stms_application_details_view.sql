drop  view IF EXISTS egswtax_applicationdetails_view;
CREATE OR REPLACE view egswtax_applicationdetails_view as 
select appDetails.id as applicationId,appDetails.applicationnumber as applicationNo,appDetails.applicationdate as applicationDate,appType.name as applicationType,appDetails.isactive as active,status.description as applicationStatus,con.id as connectionId,con.shsc_number as shscNo,con.status as connectionStatus,conndetail.propertyidentifier as propertyId,conndetail.propertytype as propertyType,case when appDetails.state_id is null then (select name from eg_user where id =appDetails.lastmodifiedby ) else (select emp.name from view_egeis_employee emp,eg_wf_states wfStates where emp.position=wfStates.owner_pos and wfStates.id=appDetails.state_id limit 1) end as processOwner,mv.ownersname as propertyOwner,mv.mobileno as mobileNo,mv.houseno as doorNo,mv.address as address,ward.name as revenueWard,zone.name as zoneName,locality.name as localityName,block.name as blockName,el_ward.name as electionWard from egswtax_connectiondetail conndetail left outer join (select * from egswtax_applicationdetails  where id in (select max(id) from egswtax_applicationdetails group by connection)) appDetails on appDetails.connectiondetail=conndetail.id left outer join egswtax_connection con on appDetails.connection=con.id,egswtax_application_type appType,egw_status status,egpt_mv_propertyinfo mv,eg_boundary ward,eg_boundary zone,eg_boundary locality,eg_boundary block,eg_boundary el_ward where appDetails.applicationtype =appType.id  and appDetails.status=status.id and conndetail.propertyidentifier=mv.upicno and mv.wardid=ward.id and mv.zoneid=zone.id and mv.blockid=block.id and mv.localityid=locality.id and mv.electionwardid=el_ward.id;



