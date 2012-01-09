<?xml version="1.0" encoding="UTF-8"?>
<!-- SEIF ami_to_mdm_request.xsl 

    Description:  This stylesheet reads in a AMI request and builds the XML request for MDM.  The output will be passed to a CDATA encapsulator
	to wrap this XML in a CDATA tag.  This is a requirement for MDM.

	 Author: Bryon Kataoka  
	History:
	10/19/2010 - updated Parameter to allow only 1 ValidationStatus1.
  -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:dp="http://www.datapower.com/extensions" xmlns:str="http://exslt.org/strings" xmlns:dpconfig="http://www.datapower.com/param/config" xmlns:date="http://exslt.org/dates-and-times" extension-element-prefixes="dp" exclude-result-prefixes="dp dpconfig">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>
	<xsl:template match="/">
		<xsl:variable name="aclaraRequest">
			<xsl:copy-of select="."/>
		</xsl:variable>
		<Task xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
			<RunTask Template="DefaultReadingXmlExportApi" ReturnType="String">
				<Parameters>
					<xsl:apply-templates select="//*[local-name()='MeterNo']"/>
					<xsl:apply-templates select="//*[local-name()='StartDate']"/>
					<xsl:apply-templates select="//*[local-name()='EndDate']"/>
					<Parameter Name="NodeType" Value="Account" Index="0"/>
					<xsl:apply-templates select="//*[local-name()='AccountNumber']"/>
					<Parameter Name="ChannelTypeID" Value="Interval" Index="1"/>
					<Parameter Name="TimeZoneID" Value="PacificUS" Index="0"/>
					<Parameter Name="AllowPartialReadings" Value="True" Index="0"/>
					<Parameter Name="OutputTimeZoneID" Value="PacificUS" Index="0"/>
					<xsl:apply-templates select="//*[local-name()='FuelType']"/>
					<Parameter Name="ReadingXmlExportAddUDAs" Value="True" Index="0"/>
					<Parameter Name="NumberOfDecimals" Value="2" Index="0"/>
					<Parameter Name="ReadingOutputRounding" Value="None" Index="0"/>
					<Parameter Name="UnitScaling" Value="KiloUnits" Index="0"/>
	<!--				<Parameter Name="ValidationStatus1" Value="VEESET1MA,VEESET1PV" Index="0"/> -->

				</Parameters>
			</RunTask>
		</Task>
	</xsl:template>
	<!-- If FuelType = "Gas" OutputIntervalLength=60, else 15 -->
	<xsl:template match="//*[local-name()='FuelType']">
		<xsl:choose>
			<xsl:when test="string(.) = 'Gas'">
				<Parameter Name="OutputIntervalLength" Value="60" Index="0"/>
			</xsl:when>
			<xsl:otherwise>
				<Parameter Name="OutputIntervalLength" Value="15" Index="0"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ******************************  match on MeterNo -->
	<xsl:template match="//*[local-name()='MeterNo']">
		<!-- Save Meter number for future filtering on response from MDM -->
		<xsl:variable name="meter" select="."/>
		<dp:set-variable name="'var://context/getMeterReadings/MeterNo'" value="string($meter)"/>
	</xsl:template>
	<!-- ******************************  match on AccountNumber -->
	<!-- Additional business logic requires a BusKeyValue placed in a DP variable for the audit log routines -->
	<xsl:template match="//*[local-name()='AccountNumber']">
	<xsl:variable name="busKey" select="."/>
		<Parameter Name="EntityID" Value="{.}" Index="0"/>
			<dp:set-variable name="'var://context/MessageIds/BusKeyValue'" value="string($busKey)"/>
	</xsl:template>
	<!-- ******************************  match on StartDate -->
	<!-- "StartDate - 1) If (request/FuelType) = ""Gas"", Add 60 minutes else Add 15 2) Add Z to end"  -->
	<xsl:template match="//*[local-name()='StartDate']">
		<xsl:variable name="fuelOption" select="//*[local-name()='FuelType']"/>
		<xsl:variable name="thisTimeStamp" select="."/>
		<xsl:choose>
			<xsl:when test="string($fuelOption) = 'Gas'">
				<!-- If (request/FuelType) = ""Gas"", Add 60 minutes and a Z at the end-->
				<xsl:variable name="timeForGas" select="concat(date:add($thisTimeStamp,date:duration(3600)),'Z')"/>
				<Parameter Name="StartDate" Value="{$timeForGas}" Index="0"/>
				<dp:set-variable name="'var://context/getMeterReadings/requestedStartDate'" value="string($thisTimeStamp)"/>
			</xsl:when>
			<xsl:otherwise>
				<!-- If (request/FuelType) = ""Electric"", Add 15 minutes and a Z at the end-->
				<xsl:variable name="timeForElectric" select="concat(date:add($thisTimeStamp,date:duration(900)),'Z')"/>
				<Parameter Name="StartDate" Value="{$timeForElectric}" Index="0"/>
				<dp:set-variable name="'var://context/getMeterReadings/requestedStartDate'" value="string($thisTimeStamp)"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ******************************  match on EndDate -->	
	<xsl:template match="//*[local-name()='EndDate']">
		<!-- "EndDate - 1) If (request/FuelType) = ""Gas"", Add 60 minutes else Add 15 2) Add Z to end"  -->
		<xsl:variable name="fuelOption" select="//*[local-name()='FuelType']"/>
		<xsl:variable name="thisTimeStamp" select="."/>
		<xsl:choose>
			<xsl:when test="string($fuelOption) = 'Gas'">
				<!-- If (request/FuelType) = ""Gas"", Add 60 minutes and a Z at the end-->
				<xsl:variable name="timeForGas" select="concat(date:add($thisTimeStamp,date:duration(3600)),'Z')"/>
				<Parameter Name="EndDate" Value="{$timeForGas}" Index="0"/>
				<dp:set-variable name="'var://context/getMeterReadings/requestedEndDate'" value="string($thisTimeStamp)"/>
			</xsl:when>
			<xsl:otherwise>
				<!-- If (request/FuelType) = ""Electric"", Add 15 minutes and a Z at the end-->
				<xsl:variable name="timeForElectric" select="concat(date:add($thisTimeStamp,date:duration(900)),'Z')"/>
				<Parameter Name="EndDate" Value="{$timeForElectric}" Index="0"/>
				<dp:set-variable name="'var://context/getMeterReadings/requestedEndDate'" value="string($thisTimeStamp)"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
