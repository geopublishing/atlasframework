<?xml version="1.0" encoding="UTF-8"?>
<sld:UserStyle xmlns="http://www.opengis.net/sld" xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
  <sld:Name>AtlasStyler v1.4, b780 (Wed Mar 10 14:23:00 CET 2010)</sld:Name>
  <sld:Title/>
  <sld:FeatureTypeStyle>
    <sld:Name>QUANTITIES_COLORIZED_POLYGON:VALUE#RANKF:NORM#null:METHOD#MANUAL:PALETTE#PiYG</sld:Name>
    <sld:FeatureTypeName>join10</sld:FeatureTypeName>
    <sld:SemanticTypeIdentifier>SemanticType[ANY]</sld:SemanticTypeIdentifier>
    <sld:Rule>
      <sld:Name>AS: 1/3 GraduatedColorPolygonRuleList</sld:Name>
      <sld:Title>en{TOP 50}</sld:Title>
      <ogc:Filter>
        <ogc:And>
          <ogc:Not>
            <ogc:Or>
              <ogc:PropertyIsNull>
                <ogc:PropertyName>RANKF</ogc:PropertyName>
              </ogc:PropertyIsNull>
              <ogc:PropertyIsEqualTo>
                <ogc:PropertyName>RANKF</ogc:PropertyName>
                <ogc:Literal>0</ogc:Literal>
              </ogc:PropertyIsEqualTo>
            </ogc:Or>
          </ogc:Not>
          <ogc:PropertyIsBetween>
            <ogc:PropertyName>RANKF</ogc:PropertyName>
            <ogc:LowerBoundary>
              <ogc:Literal>1.0</ogc:Literal>
            </ogc:LowerBoundary>
            <ogc:UpperBoundary>
              <ogc:Literal>50.0</ogc:Literal>
            </ogc:UpperBoundary>
          </ogc:PropertyIsBetween>
        </ogc:And>
      </ogc:Filter>
      <sld:MinScaleDenominator>4.9E-324</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PolygonSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </sld:Geometry>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#E9A3C9</ogc:Literal>
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
      <sld:Name>AS: 2/3 GraduatedColorPolygonRuleList</sld:Name>
      <sld:Title>en{}</sld:Title>
      <ogc:Filter>
        <ogc:And>
          <ogc:Not>
            <ogc:Or>
              <ogc:PropertyIsNull>
                <ogc:PropertyName>RANKF</ogc:PropertyName>
              </ogc:PropertyIsNull>
              <ogc:PropertyIsEqualTo>
                <ogc:PropertyName>RANKF</ogc:PropertyName>
                <ogc:Literal>0</ogc:Literal>
              </ogc:PropertyIsEqualTo>
            </ogc:Or>
          </ogc:Not>
          <ogc:PropertyIsBetween>
            <ogc:PropertyName>RANKF</ogc:PropertyName>
            <ogc:LowerBoundary>
              <ogc:Literal>50.0</ogc:Literal>
            </ogc:LowerBoundary>
            <ogc:UpperBoundary>
              <ogc:Literal>138.0</ogc:Literal>
            </ogc:UpperBoundary>
          </ogc:PropertyIsBetween>
        </ogc:And>
      </ogc:Filter>
      <sld:MinScaleDenominator>4.9E-324</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PolygonSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </sld:Geometry>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#F7F7F7</ogc:Literal>
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
      <sld:Name>AS: 3/3 GraduatedColorPolygonRuleList</sld:Name>
      <sld:Title>en{BOTTOM 50}</sld:Title>
      <ogc:Filter>
        <ogc:And>
          <ogc:Not>
            <ogc:Or>
              <ogc:PropertyIsNull>
                <ogc:PropertyName>RANKF</ogc:PropertyName>
              </ogc:PropertyIsNull>
              <ogc:PropertyIsEqualTo>
                <ogc:PropertyName>RANKF</ogc:PropertyName>
                <ogc:Literal>0</ogc:Literal>
              </ogc:PropertyIsEqualTo>
            </ogc:Or>
          </ogc:Not>
          <ogc:PropertyIsBetween>
            <ogc:PropertyName>RANKF</ogc:PropertyName>
            <ogc:LowerBoundary>
              <ogc:Literal>138.0</ogc:Literal>
            </ogc:LowerBoundary>
            <ogc:UpperBoundary>
              <ogc:Literal>188.0</ogc:Literal>
            </ogc:UpperBoundary>
          </ogc:PropertyIsBetween>
        </ogc:And>
      </ogc:Filter>
      <sld:MinScaleDenominator>4.9E-324</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PolygonSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </sld:Geometry>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#A1D76A</ogc:Literal>
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
      <sld:Name>NODATA_RULE_HIDE_IN_LEGEND</sld:Name>
      <sld:Title>en{missing data, probably very low}</sld:Title>
      <ogc:Filter>
        <ogc:Or>
          <ogc:PropertyIsNull>
            <ogc:PropertyName>RANKF</ogc:PropertyName>
          </ogc:PropertyIsNull>
          <ogc:PropertyIsEqualTo>
            <ogc:PropertyName>RANKF</ogc:PropertyName>
            <ogc:Literal>0</ogc:Literal>
          </ogc:PropertyIsEqualTo>
        </ogc:Or>
      </ogc:Filter>
      <sld:MinScaleDenominator>4.9E-324</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PolygonSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </sld:Geometry>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#FFFFFF</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="fill-opacity">
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Fill>
        <sld:Stroke>
          <sld:CssParameter name="stroke">
            <ogc:Literal>#C0C0C0</ogc:Literal>
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
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-dashoffset">
            <ogc:Literal>0.0</ogc:Literal>
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
        <ogc:And>
          <ogc:PropertyIsEqualTo>
            <ogc:Literal>ALL_LABEL_CLASSES_ENABLED</ogc:Literal>
            <ogc:Literal>ALL_LABEL_CLASSES_ENABLED</ogc:Literal>
          </ogc:PropertyIsEqualTo>
          <ogc:And>
            <ogc:PropertyIsEqualTo>
              <ogc:Literal>LABEL_CLASS_ENABLED</ogc:Literal>
              <ogc:Literal>LABEL_CLASS_ENABLED</ogc:Literal>
            </ogc:PropertyIsEqualTo>
            <ogc:And>
              <ogc:Not>
                <ogc:Or>
                  <ogc:PropertyIsEqualTo>
                    <ogc:PropertyName>RANKF</ogc:PropertyName>
                    <ogc:Literal>0</ogc:Literal>
                  </ogc:PropertyIsEqualTo>
                  <ogc:PropertyIsNull>
                    <ogc:PropertyName>RANKF</ogc:PropertyName>
                  </ogc:PropertyIsNull>
                </ogc:Or>
              </ogc:Not>
              <ogc:Not>
                <ogc:Or>
                  <ogc:Or>
                    <ogc:PropertyIsNull>
                      <ogc:PropertyName>BANDWF</ogc:PropertyName>
                    </ogc:PropertyIsNull>
                    <ogc:PropertyIsEqualTo>
                      <ogc:PropertyName>BANDWF</ogc:PropertyName>
                      <ogc:Literal>0.0</ogc:Literal>
                    </ogc:PropertyIsEqualTo>
                  </ogc:Or>
                </ogc:Or>
              </ogc:Not>
            </ogc:And>
          </ogc:And>
        </ogc:And>
      </ogc:Filter>
      <sld:MinScaleDenominator>1.0</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:TextSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </sld:Geometry>
        <sld:Label>
          <ogc:Function name="strConcat">
            <ogc:PropertyName>CNTRY_NAME</ogc:PropertyName>
            <ogc:Function name="strConcat">
              <ogc:Literal>: </ogc:Literal>
              <ogc:PropertyName>RANKF</ogc:PropertyName>
            </ogc:Function>
          </ogc:Function>
        </sld:Label>
        <sld:Font>
          <sld:CssParameter name="font-family">
            <ogc:Literal>Lucida Sans</ogc:Literal>
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
                <ogc:Literal>.5</ogc:Literal>
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
        <sld:Halo>
          <sld:Radius>
            <ogc:Literal>1.0</ogc:Literal>
          </sld:Radius>
          <sld:Fill>
            <sld:CssParameter name="fill">
              <ogc:Literal>#EEEEEE</ogc:Literal>
            </sld:CssParameter>
            <sld:CssParameter name="fill-opacity">
              <ogc:Literal>0.9</ogc:Literal>
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
          <ogc:PropertyName>POP_CNTRY</ogc:PropertyName>
        </sld:Priority>
      </sld:TextSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:Name>DONTIMPORT</sld:Name>
      <ogc:Filter>
        <ogc:And>
          <ogc:PropertyIsEqualTo>
            <ogc:Literal>ALL_LABEL_CLASSES_ENABLED</ogc:Literal>
            <ogc:Literal>ALL_LABEL_CLASSES_ENABLED</ogc:Literal>
          </ogc:PropertyIsEqualTo>
          <ogc:And>
            <ogc:PropertyIsEqualTo>
              <ogc:Literal>LABEL_CLASS_ENABLED</ogc:Literal>
              <ogc:Literal>LABEL_CLASS_ENABLED</ogc:Literal>
            </ogc:PropertyIsEqualTo>
            <ogc:And>
              <ogc:Or>
                <ogc:PropertyIsEqualTo>
                  <ogc:PropertyName>RANKF</ogc:PropertyName>
                  <ogc:Literal>0</ogc:Literal>
                </ogc:PropertyIsEqualTo>
                <ogc:PropertyIsNull>
                  <ogc:PropertyName>RANKF</ogc:PropertyName>
                </ogc:PropertyIsNull>
              </ogc:Or>
              <ogc:Not>
                <ogc:Or>
                  <ogc:Or>
                    <ogc:PropertyIsNull>
                      <ogc:PropertyName>BANDWF</ogc:PropertyName>
                    </ogc:PropertyIsNull>
                    <ogc:PropertyIsEqualTo>
                      <ogc:PropertyName>BANDWF</ogc:PropertyName>
                      <ogc:Literal>0.0</ogc:Literal>
                    </ogc:PropertyIsEqualTo>
                  </ogc:Or>
                </ogc:Or>
              </ogc:Not>
            </ogc:And>
          </ogc:And>
        </ogc:And>
      </ogc:Filter>
      <sld:MinScaleDenominator>1.0</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:TextSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </sld:Geometry>
        <sld:Label>
          <ogc:PropertyName>CNTRY_NAME</ogc:PropertyName>
        </sld:Label>
        <sld:Font>
          <sld:CssParameter name="font-family">
            <ogc:Literal>Lucida Sans</ogc:Literal>
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
                <ogc:Literal>.5</ogc:Literal>
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
        <sld:Halo>
          <sld:Radius>
            <ogc:Literal>1.0</ogc:Literal>
          </sld:Radius>
          <sld:Fill>
            <sld:CssParameter name="fill">
              <ogc:Literal>#EEEEEE</ogc:Literal>
            </sld:CssParameter>
            <sld:CssParameter name="fill-opacity">
              <ogc:Literal>0.9</ogc:Literal>
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
          <ogc:PropertyName>POP_CNTRY</ogc:PropertyName>
        </sld:Priority>
      </sld:TextSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:Name>missing data</sld:Name>
      <ogc:Filter>
        <ogc:And>
          <ogc:PropertyIsEqualTo>
            <ogc:Literal>ALL_LABEL_CLASSES_ENABLED</ogc:Literal>
            <ogc:Literal>ALL_LABEL_CLASSES_ENABLED</ogc:Literal>
          </ogc:PropertyIsEqualTo>
          <ogc:And>
            <ogc:PropertyIsEqualTo>
              <ogc:Literal>LABEL_CLASS_ENABLED</ogc:Literal>
              <ogc:Literal>LABEL_CLASS_ENABLED</ogc:Literal>
            </ogc:PropertyIsEqualTo>
            <ogc:Or>
              <ogc:PropertyIsNull>
                <ogc:PropertyName>BANDWF</ogc:PropertyName>
              </ogc:PropertyIsNull>
              <ogc:PropertyIsEqualTo>
                <ogc:PropertyName>BANDWF</ogc:PropertyName>
                <ogc:Literal>0.0</ogc:Literal>
              </ogc:PropertyIsEqualTo>
            </ogc:Or>
          </ogc:And>
        </ogc:And>
      </ogc:Filter>
      <sld:MinScaleDenominator>4.9E-324</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:TextSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </sld:Geometry>
        <sld:Label>
          <ogc:PropertyName>CNTRY_NAME</ogc:PropertyName>
        </sld:Label>
        <sld:Font>
          <sld:CssParameter name="font-family">
            <ogc:Literal>Lucida Sans</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="font-size">
            <ogc:Literal>9.0</ogc:Literal>
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
                <ogc:Literal>.5</ogc:Literal>
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
        <sld:Halo>
          <sld:Radius>
            <ogc:Literal>1.0</ogc:Literal>
          </sld:Radius>
          <sld:Fill>
            <sld:CssParameter name="fill">
              <ogc:Literal>#EEEEEE</ogc:Literal>
            </sld:CssParameter>
            <sld:CssParameter name="fill-opacity">
              <ogc:Literal>0.6</ogc:Literal>
            </sld:CssParameter>
          </sld:Fill>
        </sld:Halo>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#999999</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="fill-opacity">
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Fill>
      </sld:TextSymbolizer>
    </sld:Rule>
  </sld:FeatureTypeStyle>
</sld:UserStyle>
