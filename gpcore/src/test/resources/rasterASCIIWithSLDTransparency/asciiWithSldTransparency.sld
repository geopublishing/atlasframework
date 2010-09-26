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
        <sld:Opacity>
          <ogc:Literal>1.0</ogc:Literal>
        </sld:Opacity>
        <sld:OverlapBehavior>
          <sld:null/>
        </sld:OverlapBehavior>
        <sld:ColorMap>
          <sld:ColorMapEntry color="#000000" opacity="0.0" quantity="-9999.0"/>
          <sld:ColorMapEntry color="#CCFFFF" opacity="1.0" quantity="700.0"/>
          <sld:ColorMapEntry color="#003399" opacity="1.0" quantity="1500.0"/>
        </sld:ColorMap>
        <sld:ContrastEnhancement/>
        <sld:ShadedRelief>
          <sld:BrightnessOnly>false</sld:BrightnessOnly>
          <sld:ReliefFactor>55</sld:ReliefFactor>
        </sld:ShadedRelief>
      </sld:RasterSymbolizer>
    </sld:Rule>
  </sld:FeatureTypeStyle>
</sld:UserStyle>
