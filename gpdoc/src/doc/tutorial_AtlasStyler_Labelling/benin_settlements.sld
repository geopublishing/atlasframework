<?xml version="1.0" encoding="UTF-8"?>
<sld:UserStyle xmlns="http://www.opengis.net/sld" xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
  <sld:Name>AtlasStyler v1.3, b238 (2009/10/16 13:25)</sld:Name>
  <sld:Title>wikisquare.de</sld:Title>
  <sld:FeatureTypeStyle>
    <sld:Name>SINGLE_SYMBOL_POINT</sld:Name>
    <sld:FeatureTypeName>Feature</sld:FeatureTypeName>
    <sld:SemanticTypeIdentifier>SemanticType[ANY]</sld:SemanticTypeIdentifier>
    <sld:Rule>
      <sld:Name>AS:SinglePointSymbolRuleList</sld:Name>
      <sld:Title>settlements</sld:Title>
      <sld:MinScaleDenominator>4.9E-324</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PointSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </sld:Geometry>
        <sld:Graphic>
          <sld:Mark>
            <sld:WellKnownName>circle</sld:WellKnownName>
            <sld:Fill>
              <sld:CssParameter name="fill">
                <ogc:Literal>#e21717</ogc:Literal>
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
                <ogc:Literal>1.0</ogc:Literal>
              </sld:CssParameter>
              <sld:CssParameter name="stroke-width">
                <ogc:Literal>1</ogc:Literal>
              </sld:CssParameter>
              <sld:CssParameter name="stroke-dashoffset">
                <ogc:Literal>0.0</ogc:Literal>
              </sld:CssParameter>
            </sld:Stroke>
          </sld:Mark>
          <sld:Opacity>
            <ogc:Literal>1.0</ogc:Literal>
          </sld:Opacity>
          <sld:Size>
            <ogc:Literal>6.0</ogc:Literal>
          </sld:Size>
          <sld:Rotation>
            <ogc:Literal>0.0</ogc:Literal>
          </sld:Rotation>
        </sld:Graphic>
      </sld:PointSymbolizer>
    </sld:Rule>
  </sld:FeatureTypeStyle>
  <sld:FeatureTypeStyle>
    <sld:Name>TEXT_LABEL</sld:Name>
    <sld:FeatureTypeName>Feature</sld:FeatureTypeName>
    <sld:SemanticTypeIdentifier>SemanticType[ANY]</sld:SemanticTypeIdentifier>
    <sld:Rule>
      <sld:Name>Default/all others</sld:Name>
      <ogc:Filter>
        <ogc:And>
          <ogc:Not>
            <ogc:Or>
              <ogc:PropertyIsNull>
                <ogc:PropertyName>POP20022</ogc:PropertyName>
              </ogc:PropertyIsNull>
              <ogc:PropertyIsLike wildCard="*" singleChar="?" escape="\">
                <ogc:PropertyName>POP20022</ogc:PropertyName>
                <ogc:Literal></ogc:Literal>
              </ogc:PropertyIsLike>
            </ogc:Or>
          </ogc:Not>
          <ogc:PropertyIsEqualTo>
            <ogc:Literal>1</ogc:Literal>
            <ogc:Literal>1</ogc:Literal>
          </ogc:PropertyIsEqualTo>
        </ogc:And>
      </ogc:Filter>
      <sld:MinScaleDenominator>1.0</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:TextSymbolizer>
        <sld:Label>
          <ogc:Function name="strConcat">
            <ogc:PropertyName>NAME</ogc:PropertyName>
            <ogc:Function name="strConcat">
              <ogc:Literal>: </ogc:Literal>
              <ogc:PropertyName>POP20022</ogc:PropertyName>
            </ogc:Function>
          </ogc:Function>
        </sld:Label>
        <sld:Font>
          <sld:CssParameter name="font-family">
            <ogc:Literal>Lucida Sans</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="font-size">
            <ogc:Literal>12.0</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="font-style">
            <ogc:Literal>normal</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="font-weight">
            <ogc:Literal>bold</ogc:Literal>
          </sld:CssParameter>
        </sld:Font>
        <sld:LabelPlacement>
          <sld:PointPlacement>
            <sld:AnchorPoint>
              <sld:AnchorPointX>
                <ogc:Literal>0.0</ogc:Literal>
              </sld:AnchorPointX>
              <sld:AnchorPointY>
                <ogc:Literal>0.6</ogc:Literal>
              </sld:AnchorPointY>
            </sld:AnchorPoint>
            <sld:Displacement>
              <sld:DisplacementX>
                <ogc:Literal>5.0</ogc:Literal>
              </sld:DisplacementX>
              <sld:DisplacementY>
                <ogc:Literal>-1.0</ogc:Literal>
              </sld:DisplacementY>
            </sld:Displacement>
            <sld:Rotation>
              <ogc:Literal>0.0</ogc:Literal>
            </sld:Rotation>
          </sld:PointPlacement>
        </sld:LabelPlacement>
        <sld:Halo>
          <sld:Radius>
            <ogc:Literal>2.0</ogc:Literal>
          </sld:Radius>
          <sld:Fill>
            <sld:CssParameter name="fill">
              <ogc:Literal>#FFFFCC</ogc:Literal>
            </sld:CssParameter>
            <sld:CssParameter name="fill-opacity">
              <ogc:Literal>0.8</ogc:Literal>
            </sld:CssParameter>
          </sld:Fill>
        </sld:Halo>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#000000</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="fill-opacity">
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Fill>
        <sld:Priority>
          <ogc:PropertyName>POP2002</ogc:PropertyName>
        </sld:Priority>
      </sld:TextSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:Name>DONTIMPORT</sld:Name>
      <ogc:Filter>
        <ogc:And>
          <ogc:Or>
            <ogc:PropertyIsNull>
              <ogc:PropertyName>POP20022</ogc:PropertyName>
            </ogc:PropertyIsNull>
            <ogc:PropertyIsLike wildCard="*" singleChar="?" escape="\">
              <ogc:PropertyName>POP20022</ogc:PropertyName>
              <ogc:Literal></ogc:Literal>
            </ogc:PropertyIsLike>
          </ogc:Or>
          <ogc:PropertyIsEqualTo>
            <ogc:Literal>1</ogc:Literal>
            <ogc:Literal>1</ogc:Literal>
          </ogc:PropertyIsEqualTo>
        </ogc:And>
      </ogc:Filter>
      <sld:MinScaleDenominator>1.0</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:TextSymbolizer>
        <sld:Label>
          <ogc:PropertyName>NAME</ogc:PropertyName>
        </sld:Label>
        <sld:Font>
          <sld:CssParameter name="font-family">
            <ogc:Literal>Lucida Sans</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="font-size">
            <ogc:Literal>12.0</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="font-style">
            <ogc:Literal>normal</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="font-weight">
            <ogc:Literal>bold</ogc:Literal>
          </sld:CssParameter>
        </sld:Font>
        <sld:LabelPlacement>
          <sld:PointPlacement>
            <sld:AnchorPoint>
              <sld:AnchorPointX>
                <ogc:Literal>0.0</ogc:Literal>
              </sld:AnchorPointX>
              <sld:AnchorPointY>
                <ogc:Literal>0.6</ogc:Literal>
              </sld:AnchorPointY>
            </sld:AnchorPoint>
            <sld:Displacement>
              <sld:DisplacementX>
                <ogc:Literal>5.0</ogc:Literal>
              </sld:DisplacementX>
              <sld:DisplacementY>
                <ogc:Literal>-1.0</ogc:Literal>
              </sld:DisplacementY>
            </sld:Displacement>
            <sld:Rotation>
              <ogc:Literal>0.0</ogc:Literal>
            </sld:Rotation>
          </sld:PointPlacement>
        </sld:LabelPlacement>
        <sld:Halo>
          <sld:Radius>
            <ogc:Literal>2.0</ogc:Literal>
          </sld:Radius>
          <sld:Fill>
            <sld:CssParameter name="fill">
              <ogc:Literal>#FFFFCC</ogc:Literal>
            </sld:CssParameter>
            <sld:CssParameter name="fill-opacity">
              <ogc:Literal>0.8</ogc:Literal>
            </sld:CssParameter>
          </sld:Fill>
        </sld:Halo>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#000000</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="fill-opacity">
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Fill>
        <sld:Priority>
          <ogc:PropertyName>POP2002</ogc:PropertyName>
        </sld:Priority>
      </sld:TextSymbolizer>
    </sld:Rule>
  </sld:FeatureTypeStyle>
</sld:UserStyle>
