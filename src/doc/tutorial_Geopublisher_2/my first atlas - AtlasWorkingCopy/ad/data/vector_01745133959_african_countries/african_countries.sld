<?xml version="1.0" encoding="UTF-8"?>
<sld:UserStyle xmlns="http://www.opengis.net/sld" xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
  <sld:Name>AtlasStyler v1.3, b506 (Tue Dec 01 19:58:00 CET 2009)</sld:Name>
  <sld:Title/>
  <sld:FeatureTypeStyle>
    <sld:Name>UNIQUE_VALUE_POLYGON</sld:Name>
    <sld:FeatureTypeName>african_countries</sld:FeatureTypeName>
    <sld:SemanticTypeIdentifier>SemanticType[ANY]</sld:SemanticTypeIdentifier>
    <sld:Rule>
      <sld:Title>de{Benin}default{2}fr{Benin}en{Benin}</sld:Title>
      <ogc:Filter>
        <ogc:PropertyIsEqualTo>
          <ogc:PropertyName>ID2</ogc:PropertyName>
          <ogc:Literal>2</ogc:Literal>
        </ogc:PropertyIsEqualTo>
      </ogc:Filter>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PolygonSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </sld:Geometry>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#fafa82</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="fill-opacity">
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Fill>
        <sld:Stroke>
          <sld:CssParameter name="stroke">
            <ogc:Literal>#000000</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-linecap">
            <ogc:Literal>butt</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-linejoin">
            <ogc:Literal>miter</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-opacity">
            <ogc:Literal>1</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-width">
            <ogc:Literal>1</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-dashoffset">
            <ogc:Literal>0</ogc:Literal>
          </sld:CssParameter>
        </sld:Stroke>
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:Title>de{Andere Länder}default{0}fr{Autre Pays}en{Other Countries}</sld:Title>
      <ogc:Filter>
        <ogc:PropertyIsEqualTo>
          <ogc:PropertyName>ID2</ogc:PropertyName>
          <ogc:Literal>0</ogc:Literal>
        </ogc:PropertyIsEqualTo>
      </ogc:Filter>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PolygonSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </sld:Geometry>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#fbed92</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="fill-opacity">
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Fill>
        <sld:Stroke>
          <sld:CssParameter name="stroke">
            <ogc:Literal>#000000</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-linecap">
            <ogc:Literal>butt</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-linejoin">
            <ogc:Literal>miter</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-opacity">
            <ogc:Literal>1</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-width">
            <ogc:Literal>1</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-dashoffset">
            <ogc:Literal>0</ogc:Literal>
          </sld:CssParameter>
        </sld:Stroke>
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:Title>de{Ozean}default{1}fr{Océan}en{Ocean}</sld:Title>
      <ogc:Filter>
        <ogc:PropertyIsEqualTo>
          <ogc:PropertyName>ID2</ogc:PropertyName>
          <ogc:Literal>1</ogc:Literal>
        </ogc:PropertyIsEqualTo>
      </ogc:Filter>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PolygonSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </sld:Geometry>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#33ccff</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="fill-opacity">
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Fill>
        <sld:Stroke>
          <sld:CssParameter name="stroke">
            <ogc:Literal>#000000</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-linecap">
            <ogc:Literal>butt</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-linejoin">
            <ogc:Literal>miter</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-opacity">
            <ogc:Literal>1</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-width">
            <ogc:Literal>1</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-dashoffset">
            <ogc:Literal>0</ogc:Literal>
          </sld:CssParameter>
        </sld:Stroke>
      </sld:PolygonSymbolizer>
    </sld:Rule>
  </sld:FeatureTypeStyle>
  <sld:FeatureTypeStyle>
    <sld:Name>TEXT_LABEL</sld:Name>
    <sld:FeatureTypeName>Feature</sld:FeatureTypeName>
    <sld:SemanticTypeIdentifier>SemanticType[ANY]</sld:SemanticTypeIdentifier>
    <sld:Rule>
      <sld:Name>Default/all others</sld:Name>
      <ogc:Filter>
        <ogc:PropertyIsEqualTo>
          <ogc:Literal>1</ogc:Literal>
          <ogc:Literal>1</ogc:Literal>
        </ogc:PropertyIsEqualTo>
      </ogc:Filter>
      <sld:TextSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </sld:Geometry>
        <sld:Label>
          <ogc:PropertyName>CNTRY_NAME</ogc:PropertyName>
        </sld:Label>
        <sld:Font>
          <sld:CssParameter name="font-family">
            <ogc:Literal>Times New Roman</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="font-size">
            <ogc:Literal>11.0</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="font-style">
            <ogc:Literal>normal</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="font-weight">
            <ogc:Literal>normal</ogc:Literal>
          </sld:CssParameter>
        </sld:Font>
        <sld:LabelPlacement>
          <sld:PointPlacement>
            <sld:AnchorPoint>
              <sld:AnchorPointX>
                <ogc:Literal>0.0</ogc:Literal>
              </sld:AnchorPointX>
              <sld:AnchorPointY>
                <ogc:Literal>0.5</ogc:Literal>
              </sld:AnchorPointY>
            </sld:AnchorPoint>
            <sld:Displacement>
              <sld:DisplacementX>
                <ogc:Literal>0.0</ogc:Literal>
              </sld:DisplacementX>
              <sld:DisplacementY>
                <ogc:Literal>0.0</ogc:Literal>
              </sld:DisplacementY>
            </sld:Displacement>
            <sld:Rotation>
              <ogc:Literal>0</ogc:Literal>
            </sld:Rotation>
          </sld:PointPlacement>
        </sld:LabelPlacement>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#000000</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="fill-opacity">
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Fill>
      </sld:TextSymbolizer>
    </sld:Rule>
  </sld:FeatureTypeStyle>
</sld:UserStyle>
