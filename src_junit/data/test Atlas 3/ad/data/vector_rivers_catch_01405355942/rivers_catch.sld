<?xml version="1.0" encoding="ISO-8859-1" standalone="yes"?>
<sld:StyledLayerDescriptor version="1.0.0" xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:xlink="http://www.w3.org/1999/xlink">
  <sld:NamedLayer>
    <sld:Name>rivers_catch</sld:Name>
    <sld:UserStyle>
      <sld:Name>Style1</sld:Name>
      <sld:FeatureTypeStyle>
        <sld:FeatureTypeName>rivers_catch</sld:FeatureTypeName>
        <sld:Rule>

<MinScaleDenominator>1</MinScaleDenominator>
<MaxScaleDenominator>1e7</MaxScaleDenominator>

          <sld:Name>3 - 100</sld:Name>
          <sld:Title>3 - 100</sld:Title>
          <ogc:Filter>
            <ogc:PropertyIsBetween>
              <ogc:PropertyName>SHREVE</ogc:PropertyName>
              <ogc:LowerBoundary>
                <ogc:Literal>3</ogc:Literal>
              </ogc:LowerBoundary>
              <ogc:UpperBoundary>
                <ogc:Literal>100</ogc:Literal>
              </ogc:UpperBoundary>
            </ogc:PropertyIsBetween>
          </ogc:Filter>
          <sld:LineSymbolizer>
            <sld:Stroke>
              <sld:CssParameter name="stroke">#99DDFF</sld:CssParameter>
              <sld:CssParameter name="stroke-width">0.6</sld:CssParameter>
              <sld:CssParameter name="stroke-opacity">1</sld:CssParameter>
            </sld:Stroke>
          </sld:LineSymbolizer>
        </sld:Rule>
        <sld:Rule>

<MinScaleDenominator>1</MinScaleDenominator>
<MaxScaleDenominator>1e7</MaxScaleDenominator>

          <sld:Name>101 - 350</sld:Name>
          <sld:Title>101 - 350</sld:Title>
          <ogc:Filter>
            <ogc:PropertyIsBetween>
              <ogc:PropertyName>SHREVE</ogc:PropertyName>
              <ogc:LowerBoundary>
                <ogc:Literal>101</ogc:Literal>
              </ogc:LowerBoundary>
              <ogc:UpperBoundary>
                <ogc:Literal>350</ogc:Literal>
              </ogc:UpperBoundary>
            </ogc:PropertyIsBetween>
          </ogc:Filter>
          <sld:LineSymbolizer>
            <sld:Stroke>
              <sld:CssParameter name="stroke">#80D4FF</sld:CssParameter>
              <sld:CssParameter name="stroke-width">0.6</sld:CssParameter>
              <sld:CssParameter name="stroke-opacity">1</sld:CssParameter>
            </sld:Stroke>
          </sld:LineSymbolizer>
        </sld:Rule>
        <sld:Rule>

<MinScaleDenominator>1</MinScaleDenominator>
<MaxScaleDenominator>1e7</MaxScaleDenominator>

          <sld:Name>351 - 500</sld:Name>
          <sld:Title>351 - 500</sld:Title>
          <ogc:Filter>
            <ogc:PropertyIsBetween>
              <ogc:PropertyName>SHREVE</ogc:PropertyName>
              <ogc:LowerBoundary>
                <ogc:Literal>351</ogc:Literal>
              </ogc:LowerBoundary>
              <ogc:UpperBoundary>
                <ogc:Literal>500</ogc:Literal>
              </ogc:UpperBoundary>
            </ogc:PropertyIsBetween>
          </ogc:Filter>
          <sld:LineSymbolizer>
            <sld:Stroke>
              <sld:CssParameter name="stroke">#5CA3E6</sld:CssParameter>
              <sld:CssParameter name="stroke-width">0.6</sld:CssParameter>
              <sld:CssParameter name="stroke-opacity">1</sld:CssParameter>
            </sld:Stroke>
          </sld:LineSymbolizer>
        </sld:Rule>
        <sld:Rule>
          <sld:Name>501 - 750</sld:Name>
          <sld:Title>501 - 750</sld:Title>
          <ogc:Filter>
            <ogc:PropertyIsBetween>
              <ogc:PropertyName>SHREVE</ogc:PropertyName>
              <ogc:LowerBoundary>
                <ogc:Literal>501</ogc:Literal>
              </ogc:LowerBoundary>
              <ogc:UpperBoundary>
                <ogc:Literal>750</ogc:Literal>
              </ogc:UpperBoundary>
            </ogc:PropertyIsBetween>
          </ogc:Filter>
          <sld:LineSymbolizer>
            <sld:Stroke>
              <sld:CssParameter name="stroke">#1F83E0</sld:CssParameter>
              <sld:CssParameter name="stroke-width">0.6</sld:CssParameter>
              <sld:CssParameter name="stroke-opacity">1</sld:CssParameter>
            </sld:Stroke>
          </sld:LineSymbolizer>
        </sld:Rule>
        <sld:Rule>

<MinScaleDenominator>1</MinScaleDenominator>
<MaxScaleDenominator>1e7</MaxScaleDenominator>

          <sld:Name>751 - 1000</sld:Name>
          <sld:Title>751 - 1000</sld:Title>
          <ogc:Filter>
            <ogc:PropertyIsBetween>
              <ogc:PropertyName>SHREVE</ogc:PropertyName>
              <ogc:LowerBoundary>
                <ogc:Literal>751</ogc:Literal>
              </ogc:LowerBoundary>
              <ogc:UpperBoundary>
                <ogc:Literal>1000</ogc:Literal>
              </ogc:UpperBoundary>
            </ogc:PropertyIsBetween>
          </ogc:Filter>
          <sld:LineSymbolizer>
            <sld:Stroke>
              <sld:CssParameter name="stroke">#2259C7</sld:CssParameter>
              <sld:CssParameter name="stroke-width">0.8</sld:CssParameter>
              <sld:CssParameter name="stroke-opacity">1</sld:CssParameter>
            </sld:Stroke>
          </sld:LineSymbolizer>
        </sld:Rule>
        <sld:Rule>

<MinScaleDenominator>1</MinScaleDenominator>
<MaxScaleDenominator>1e7</MaxScaleDenominator>

          <sld:Name>1001 - 2500</sld:Name>
          <sld:Title>1001 - 2500</sld:Title>
          <ogc:Filter>
            <ogc:PropertyIsBetween>
              <ogc:PropertyName>SHREVE</ogc:PropertyName>
              <ogc:LowerBoundary>
                <ogc:Literal>1001</ogc:Literal>
              </ogc:LowerBoundary>
              <ogc:UpperBoundary>
                <ogc:Literal>2500</ogc:Literal>
              </ogc:UpperBoundary>
            </ogc:PropertyIsBetween>
          </ogc:Filter>
          <sld:LineSymbolizer>
            <sld:Stroke>
              <sld:CssParameter name="stroke">#1A32AB</sld:CssParameter>
              <sld:CssParameter name="stroke-width">1.4</sld:CssParameter>
              <sld:CssParameter name="stroke-opacity">1</sld:CssParameter>
            </sld:Stroke>
          </sld:LineSymbolizer>
        </sld:Rule>
        <sld:Rule>

<MinScaleDenominator>1</MinScaleDenominator>
<MaxScaleDenominator>1e7</MaxScaleDenominator>

          <sld:Name>2501 - 5078</sld:Name>
          <sld:Title>2501 - 5078</sld:Title>
          <ogc:Filter>
            <ogc:PropertyIsBetween>
              <ogc:PropertyName>SHREVE</ogc:PropertyName>
              <ogc:LowerBoundary>
                <ogc:Literal>2501</ogc:Literal>
              </ogc:LowerBoundary>
              <ogc:UpperBoundary>
                <ogc:Literal>5078</ogc:Literal>
              </ogc:UpperBoundary>
            </ogc:PropertyIsBetween>
          </ogc:Filter>
          <sld:LineSymbolizer>
            <sld:Stroke>
              <sld:CssParameter name="stroke">#0A0A91</sld:CssParameter>
              <sld:CssParameter name="stroke-width">1.6</sld:CssParameter>
              <sld:CssParameter name="stroke-opacity">1</sld:CssParameter>
            </sld:Stroke>
          </sld:LineSymbolizer>
        </sld:Rule>
      </sld:FeatureTypeStyle>
    </sld:UserStyle>
  </sld:NamedLayer>
</sld:StyledLayerDescriptor>
