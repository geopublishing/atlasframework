<?xml version="1.0" encoding="UTF-8"?>
<sld:UserStyle xmlns="http://www.opengis.net/sld" xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
  <sld:Name>unnamed raster style</sld:Name>
  <sld:Title>untitled raster style</sld:Title>
  <sld:FeatureTypeStyle>
    <sld:Name>name</sld:Name>
    <sld:Title>title</sld:Title>
    <sld:Abstract>abstract</sld:Abstract>
    <sld:FeatureTypeName>Feature</sld:FeatureTypeName>
    <sld:SemanticTypeIdentifier>generic:geometry</sld:SemanticTypeIdentifier>
    <sld:Rule>
      <sld:RasterSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>geom</ogc:PropertyName>
        </sld:Geometry>
        <sld:OverlapBehavior>
          <sld:null/>
        </sld:OverlapBehavior>
        <sld:ColorMap>
          <sld:ColorMapEntry color="#FFFFFF" opacity="0.0" quantity="0.0" label=""/>
          <sld:ColorMapEntry color="#7E4206" opacity="1.0" quantity="1.0"/>
          <sld:ColorMapEntry color="#D7D7B1" opacity="1.0" quantity="6.0"/>
          <sld:ColorMapEntry color="#028686" opacity="1.0" quantity="12.0"/>
          <sld:ColorMapEntry color="#FFFF66" opacity="1.0" quantity="13.0"/>
        </sld:ColorMap>
        <sld:ShadedRelief>
          <sld:BrightnessOnly>false</sld:BrightnessOnly>
        </sld:ShadedRelief>
      </sld:RasterSymbolizer>
    </sld:Rule>
  </sld:FeatureTypeStyle>
</sld:UserStyle>
