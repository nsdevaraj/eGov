DROP VIEW egwtr_mv_dcb_view;
DROP TABLE EGPT_MV_COLLECTIONREPORT;
DROP TABLE EGPT_MV_COLLECTIONREPORTDETAILS;
DROP TABLE egpt_mv_current_prop_det;
DROP TABLE egpt_mv_propertyinfo;

--egpt_mv_current_prop_det
CREATE TABLE egpt_mv_current_prop_det AS
 SELECT prop.id_property,
    propdet.id AS id_property_detail,
    propdet.id_propertytypemaster,
    propdet.id_usg_mstr,
    propdet.sital_area,
    propdet.total_builtup_area,
        propdet.category_type
   FROM egpt_mv_current_property prop,
    egpt_property_detail propdet
  WHERE prop.id_property = propdet.id_property;


CREATE TABLE egpt_mv_propertyinfo AS
 SELECT bp.id AS basicpropertyid,
    bp.propertyid AS upicno,
    ownername(bp.id) AS ownersname,
    getAadharno(bp.id) AS aadharno,
    addr.housenobldgapt AS houseno,
    mobilenumber(bp.id) AS mobileno,
    addr.address,
    propdet.id_propertytypemaster AS proptymaster,
    propid.ward_adm_id AS wardid,
    propid.zone_num AS zoneid,
    propid.adm3 AS streetid,
    propid.adm1 AS blockid,
    propid.adm2 AS localityid,
    propid.elect_bndry AS electionwardid,
    1 AS source_id,
    propdet.sital_area,
    propdet.total_builtup_area,
    bp.status AS latest_status,
    dmdcoll.aggregate_current_demand,
    dmdcoll.aggregate_arrear_demand,
    dmdcoll.current_collection,
    dmdcoll.arrearcollection,
    bp.gis_ref_no AS gisrefno,
    prop.isexemptedfromtax AS isexempted,
    propdet.category_type AS usage,
    bp.source,
        CASE dmdcalc.alv
            WHEN NULL::double precision THEN 0::double precision
            ELSE dmdcalc.alv
        END AS alv,
        bp.longitude as longitude,
        bp.latitude as latitude,
    bp.isactive as isactive
   FROM egpt_basic_property bp,
    egpt_mv_current_property prop,
    egpt_mv_bp_address addr,
    egpt_mv_current_prop_det propdet,
    egpt_propertyid propid,
    egpt_mv_current_arrear_dem_coll dmdcoll,
    egpt_mv_curr_dmdcalc dmdcalc
  WHERE bp.propertyid IS NOT NULL AND bp.addressid::numeric = addr.addressid AND prop.id_basic_property = bp.id AND propdet.id_property = prop.
id_property
  AND propid.id = bp.id_propertyid AND dmdcoll.basicpropertyid = bp.id AND dmdcalc.id_basic_property = bp.id;
  
  ---EGPT_MV_COLLECTIONREPORT---
CREATE TABLE EGPT_MV_COLLECTIONREPORT AS SELECT 
receiptHeaderId,PAYMENT_MODE,RECEIPT_NUMBER,RECEIPT_DATE,TAX_COLL,PROPERTYID,WARDID,
ZONEID,AREAID,LOCALITYID,streetId,IDPROPERTY,PAYEENAME,COLLECTIONTYPE,userId,HOUSENUMBER,paidAt,status from 
      (SELECT  collectionHeader.id AS receiptHeaderId,
      (CASE WHEN instrumentType.TYPE ='cash' THEN 'cash' WHEN instrumentType.TYPE='bank' THEN 'bank' WHEN  instrumentType.TYPE='card' THEN 'card' ELSE 'cheque/dd' END)  AS PAYMENT_MODE,
      collectionHeader.receiptnumber AS RECEIPT_NUMBER,
      collectionHeader.receiptdate AS RECEIPT_DATE,
      collectionHeader.totalamount AS TAX_COLL,
      propertyInfo.upicno AS PROPERTYID,
      propertyInfo.wardid AS WARDID,
      propertyInfo.zoneid AS ZONEID,
      propertyInfo.blockid AS AREAID,
      propertyInfo.localityid AS LOCALITYID,
      propertyInfo.streetid AS streetId,
      property.id AS IDPROPERTY,
      collectionHeader.paidby AS PAYEENAME,
      collectionHeader.collectiontype AS COLLECTIONTYPE,
      userdetail.id AS userId,
      propertyInfo.houseno AS HOUSENUMBER,
      collectionHeader.source AS paidAt,
      status.id AS status
FROM EGPT_PROPERTY property,
     EGPT_MV_PROPERTYINFO propertyInfo,
     EGCL_COLLECTIONHEADER collectionHeader,
     EGCL_SERVICEDETAILS serviceDetails,
     EG_USER userdetail,
     EGW_STATUS status,
     egcl_collectioninstrument instrument,
     egf_instrumentheader instrumentHeader,
     egf_instrumenttype instrumentType
WHERE property.id_basic_property = propertyInfo.BASICPROPERTYID and 
      propertyInfo.upicno = collectionHeader.consumerCode and 
      status.id = collectionHeader.status and
      serviceDetails.id = collectionHeader.servicedetails and
      collectionHeader.createdby = userdetail.id and 
      instrument.collectionheader = collectionHeader.id and 
      instrumentHeader.id = instrument.instrumentheader and 
      instrumentType.id = instrumentHeader.instrumenttype and 
      serviceDetails.name = 'Property Tax' and 
      property.status in ('I','A')) AS RESULT
      group by RESULT.receiptHeaderId,
      RESULT.PAYMENT_MODE,
      RESULT.RECEIPT_NUMBER,
      RESULT.RECEIPT_DATE,
      RESULT.TAX_COLL,
      RESULT.PROPERTYID,
      RESULT.WARDID,
      RESULT.ZONEID,
      RESULT.AREAID ,
      RESULT.LOCALITYID ,
      RESULT.streetid ,
      RESULT.IDPROPERTY,
      RESULT.PAYEENAME,
      RESULT.COLLECTIONTYPE ,
      RESULT.userId,
      RESULT.HOUSENUMBER ,
      RESULT.paidAt,
      RESULT.status;
      
 ---EGPT_MV_COLLECTIONREPORTDETAILS---
CREATE TABLE EGPT_MV_COLLECTIONREPORTDETAILS AS SELECT
      collectionHeader.receiptHeaderId AS receiptHeaderId,
      arrearCollection.arrearAmount  AS arreartax_coll,
      currCollection.currentAmount AS currenttax_coll,
      currPenaltyCollection.penaltyAmount AS PENALTY_COLL,
      arrLibCollection.libAmount AS arrearlibrarycess_coll,
      currLibCollection.libAmount AS librarycess_coll,
      arreaPenaltyCollection.penaltyAmount AS arrearpenalty_coll
FROM EGPT_MV_COLLECTIONREPORT collectionHeader 
     left outer join EGPT_MV_ARREARCOLLECTION arrearCollection on collectionHeader.receiptHeaderId = arrearCollection.headerId
     left outer join EGPT_MV_CURRENTCOLLECTION currCollection on collectionHeader.receiptHeaderId = currCollection.headerId
     left outer join EGPT_MV_ARREARPENALTYCOLLECTION arreaPenaltyCollection on collectionHeader.receiptHeaderId = arreaPenaltyCollection.headerId
     left outer join EGPT_MV_ARREARLIBCOLLECTION arrLibCollection on collectionHeader.receiptHeaderId = arrLibCollection.headerId
     left outer join EGPT_MV_CURRENTLIBCOLLECTION currLibCollection on collectionHeader.receiptHeaderId = currLibCollection.headerId
     left outer join EGPT_MV_CURRENTPENALTYCOLLECTION currPenaltyCollection on collectionHeader.receiptHeaderId = currPenaltyCollection.headerId
     group by collectionHeader.receiptHeaderId,arrearCollection.arrearAmount,currCollection.currentAmount,currPenaltyCollection.penaltyAmount,
              arrLibCollection.libAmount,currLibCollection.libAmount,arreaPenaltyCollection.penaltyAmount;
              
              
 CREATE OR REPLACE VIEW egwtr_mv_dcb_view AS 
SELECT mvp.upicno as propertyid,
  mvp.address,
  con.consumercode AS hscno,
  mvp.ownersname   AS username,
  mvp.zoneid       AS zoneid,
  mvp.wardid       AS wardid,
  mvp.blockid      AS block,
  mvp.localityid   AS locality,
  mvp.streetid     AS street,
  cd.connectiontype,
  currdd.amount                                          AS curr_demand,
  currdd.amt_collected                                   AS curr_coll,
  currdd.amount::DOUBLE PRECISION - currdd.amt_collected AS curr_balance,
  COALESCE(0, 0)                                         AS arr_demand,
  COALESCE(0, 0)                                         AS arr_coll,
  COALESCE(0, 0)                                         AS arr_balance
FROM egwtr_connection con
JOIN egwtr_connectiondetails cd
ON con.id = cd.connection
JOIN egpt_mv_propertyinfo mvp
ON con.propertyidentifier::text = mvp.upicno::text
JOIN eg_demand currdmd
ON currdmd.id = cd.demand
LEFT JOIN eg_demand_details currdd
ON currdd.id_demand = currdmd.id
LEFT JOIN eg_demand_reason dr
ON dr.id = currdd.id_demand_reason
LEFT JOIN eg_demand_reason_master drm
ON drm.id = dr.id_demand_reason_master
LEFT JOIN eg_installment_master im
ON im.id = dr.id_installment
LEFT JOIN eg_module m
ON m.id                         = im.id_module
WHERE cd.connectionstatus::text = 'ACTIVE'::text
AND drm.code::text              = 'WTAXCHARGES'::text
AND drm.isdemand                = true
AND im.start_date              <= now()
AND im.end_date                >= now()
AND m.name::text                = 'Water Tax Management'::text
AND im.installment_type::text   = 'Monthly'::text
AND cd.connectiontype::text     = 'METERED'::text
UNION
SELECT mvp.upicno,
  mvp.address,
  con.consumercode AS hscno,
  mvp.ownersname   AS username,
  mvp.zoneid       AS zoneid,
  mvp.wardid       AS wardid,
  mvp.blockid      AS block,
  mvp.localityid   AS locality,
  mvp.streetid     AS street,
  cd.connectiontype,
  COALESCE(0, 0)                                                                      AS curr_demand,
  COALESCE(0, 0)                                                                      AS curr_coll,
  COALESCE(0, 0)                                                                      AS curr_balance,
  COALESCE(arrdd.amount, 0::bigint)                                                   AS arr_demand,
  COALESCE(arrdd.amt_collected, 0::DOUBLE PRECISION)                                  AS arr_coll,
  COALESCE(arrdd.amount::DOUBLE PRECISION - arrdd.amt_collected, 0::DOUBLE PRECISION) AS arr_balance
FROM egwtr_connection con
JOIN egwtr_connectiondetails cd
ON con.id = cd.connection
JOIN egpt_mv_propertyinfo mvp
ON con.propertyidentifier::text = mvp.upicno::text
JOIN eg_demand arrdmd
ON arrdmd.id = cd.demand
LEFT JOIN eg_demand_details arrdd
ON arrdd.id_demand = arrdmd.id
LEFT JOIN eg_demand_reason dr
ON dr.id = arrdd.id_demand_reason
LEFT JOIN eg_demand_reason_master drm
ON drm.id                       = dr.id_demand_reason_master
WHERE cd.connectionstatus::text = 'ACTIVE'::text
AND drm.code::text              = 'WTAXCHARGES'::text
AND NOT (dr.id_installment     IN
  (SELECT eim.id
  FROM eg_installment_master eim
  WHERE eim.start_date <= now()
  AND eim.end_date     >= now()
  AND (eim.id_module   IN
    (SELECT em.id
    FROM eg_module em
    WHERE em.name::text = ANY (ARRAY['Water Tax Management'::text, 'Property Tax'::text])
    ))
  ))
AND cd.connectiontype::text = 'METERED'::text
UNION
  (SELECT mvp.upicno,
    mvp.address,
    con.consumercode AS hscno,
    mvp.ownersname   AS username,
    mvp.zoneid       AS zoneid,
    mvp.wardid       AS wardid,
    mvp.blockid      AS block,
    mvp.localityid   AS locality,
    mvp.streetid     AS street,
    cd.connectiontype,
    COALESCE(currdd.amount, 0::bigint)                                                    AS curr_demand,
    COALESCE(currdd.amt_collected, 0::DOUBLE PRECISION)                                   AS curr_coll,
    COALESCE(currdd.amount::DOUBLE PRECISION - currdd.amt_collected, 0::DOUBLE PRECISION) AS curr_balance,
    COALESCE(0, 0)                                                                        AS arr_demand,
    COALESCE(0, 0)                                                                        AS arr_coll,
    COALESCE(0, 0)                                                                        AS arr_balance
  FROM egwtr_connection con
  JOIN egwtr_connectiondetails cd
  ON con.id = cd.connection
  JOIN egpt_mv_propertyinfo mvp
  ON con.propertyidentifier::text = mvp.upicno::text
  JOIN eg_demand currdmd
  ON currdmd.id = cd.demand
  LEFT JOIN eg_demand_details currdd
  ON currdd.id_demand = currdmd.id
  LEFT JOIN eg_demand_reason dr
  ON dr.id = currdd.id_demand_reason
  LEFT JOIN eg_demand_reason_master drm
  ON drm.id = dr.id_demand_reason_master
  LEFT JOIN eg_installment_master im
  ON im.id = dr.id_installment
  LEFT JOIN eg_module m
  ON m.id                         = im.id_module
  WHERE cd.connectionstatus::text = 'ACTIVE'::text
  AND drm.code::text              = 'WTAXCHARGES'::text
  AND drm.isdemand                = true
  AND im.start_date              <= now()
  AND im.end_date                >= now()
  AND m.name::text                = 'Property Tax'::text
  AND cd.connectiontype::text     = 'NON_METERED'::text
  UNION
  SELECT mvp.upicno,
    mvp.address,
    con.consumercode AS hscno,
    mvp.ownersname   AS username,
    mvp.zoneid       AS zoneid,
    mvp.wardid       AS wardid,
    mvp.blockid      AS block,
    mvp.localityid   AS locality,
    mvp.streetid     AS street,
    cd.connectiontype,
    COALESCE(0, 0)                                                                      AS curr_demand,
    COALESCE(0, 0)                                                                      AS curr_coll,
    COALESCE(0, 0)                                                                      AS curr_balance,
    COALESCE(arrdd.amount, 0::bigint)                                                   AS arr_demand,
    COALESCE(arrdd.amt_collected, 0::DOUBLE PRECISION)                                  AS arr_coll,
    COALESCE(arrdd.amount::DOUBLE PRECISION - arrdd.amt_collected, 0::DOUBLE PRECISION) AS arr_balance
  FROM egwtr_connection con
  JOIN egwtr_connectiondetails cd
  ON con.id = cd.connection
  JOIN egpt_mv_propertyinfo mvp
  ON con.propertyidentifier::text = mvp.upicno::text
  JOIN eg_demand arrdmd
  ON arrdmd.id = cd.demand
  JOIN eg_demand_details arrdd
  ON arrdd.id_demand = arrdmd.id
  LEFT JOIN eg_demand_reason dr
  ON dr.id = arrdd.id_demand_reason
  LEFT JOIN eg_demand_reason_master drm
  ON drm.id                       = dr.id_demand_reason_master
  WHERE cd.connectionstatus::text = 'ACTIVE'::text
  AND drm.code::text              = 'WTAXCHARGES'::text
  AND NOT (dr.id_installment     IN
    (SELECT eim.id
    FROM eg_installment_master eim
    WHERE eim.start_date <= now()
    AND eim.end_date     >= now()
    AND (eim.id_module   IN
      (SELECT em.id
      FROM eg_module em
      WHERE em.name::text = 'Property Tax'::text
      ))
    ))
  AND cd.connectiontype::text = 'NON_METERED'::text
  );