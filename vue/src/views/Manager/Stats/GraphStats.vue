<template>
  <div class="graph-stats-container">
    <el-card>
      <template #header>
        <span class="title">Graph Node Statistics</span>
      </template>

      <!-- Chart container using flex layout to display two charts side by side -->
      <div class="chart-container">
        <!-- Bar chart -->
        <div id="node-chart" class="chart-item"></div>
        <!-- Pie chart -->
        <div id="pie-chart" class="chart-item"></div>
      </div>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span class="title">Herb Classification Statistics</span>
      </template>

      <!-- Level1 and Level2 relationship chart -->
      <div class="chart-section">
        <h3 style="text-align: center; margin: 10px 0;">Level 1 vs Level 2 Classification Relationship</h3>
        <div class="chart-container">
          <!-- Level1 and Level2 relationship tree chart (adjusted width and height) -->
          <div id="category-tree-chart" style="width: 100%; height: 700px;"></div>
        </div>
      </div>

      <!-- Level1 pie chart and Level2 herb count bar chart on the same row -->
      <div class="chart-section">
        <h3 style="text-align: center; margin: 10px 0;">Detailed Classification Statistics</h3>
        <div class="chart-container">
          <!-- Level1 pie chart -->
          <div id="level1-pie-chart" style="width: 50%; height: 500px;"></div>
          <!-- Level2 herb count bar chart -->
          <div id="level2-herb-count-chart" style="width: 50%; height: 500px;"></div>
        </div>
      </div>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span class="title">Herb Property Statistics</span>
      </template>

      <!-- Herb property charts -->
      <div class="chart-section">
        <h3 style="text-align: center; margin: 10px 0;">Property Distribution</h3>
        <div class="chart-container">
          <!-- Property pie chart -->
          <div id="property-pie-chart" class="chart-item"></div>
          <!-- Property bar chart -->
          <div id="property-bar-chart" class="chart-item"></div>
        </div>
      </div>

      <div class="chart-section">
        <h3 style="text-align: center; margin: 10px 0;">Taste Distribution</h3>
        <div class="chart-container">
          <!-- Taste rose chart -->
          <div id="taste-rose-chart" class="chart-item"></div>
          <!-- Taste bar chart -->
          <div id="taste-bar-chart" class="chart-item"></div>
        </div>
      </div>

      <div class="chart-section">
        <h3 style="text-align: center; margin: 10px 0;">Channel Tropism Distribution</h3>
        <div class="chart-container">
          <!-- Channel funnel chart -->
          <div id="channel-funnel-chart" class="chart-item"></div>
          <!-- Channel radar chart -->
          <div id="channel-radar-chart" class="chart-item"></div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import request from '@/utils/request'

export default {
  name: 'GraphStats',
  data() {
    return {
      barChartInstance: null,
      pieChartInstance: null,
      categoryTreeChartInstance: null,
      level1PieChartInstance: null,
      level2HerbCountChartInstance: null,
      propertyPieChartInstance: null,
      propertyBarChartInstance: null,
      tasteRoseChartInstance: null,
      tasteBarChartInstance: null,
      channelFunnelChartInstance: null,
      channelRadarChartInstance: null,
      nodeData: [],
      herbPropertiesData: [],
      categoryData: []
    }
  },
  mounted() {
    this.loadChartData()
    this.loadHerbPropertiesData()
    this.loadCategoryData()
  },
  methods: {
    loadChartData() {
      request.get('/api/stats/node/total').then(res => {
        if (res && Array.isArray(res)) {
          this.nodeData = res
          this.renderBarChart()
          this.renderPieChart()
        } else {
          this.$message.error('Failed to retrieve graph data')
        }
      })
    },
    loadHerbPropertiesData() {
      request.get('/api/stats/herb/properties').then(res => {
        if (res && Array.isArray(res)) {
          this.herbPropertiesData = res
          this.renderPropertyCharts()
          this.renderTasteCharts()
          this.renderChannelCharts()
        } else {
          this.$message.error('Failed to retrieve herb property data')
        }
      })
    },
    loadCategoryData() {
      request.get('/api/stats/node/labels').then(res => {
        if (res && Array.isArray(res)) {
          this.categoryData = res
          this.renderCategoryCharts()
        } else {
          this.$message.error('Failed to retrieve classification data')
        }
      })
    },
    renderBarChart() {
      this.$nextTick(() => {
        if (!this.barChartInstance) {
          this.barChartInstance = echarts.init(document.getElementById('node-chart'))
        }

        const labels = this.nodeData.map(item => item.label)
        const counts = this.nodeData.map(item => item.count)

        // Light color array with transparency effect
        const colors = [
          'rgba(102, 184, 222, 0.7)',  // light blue
          'rgba(143, 205, 137, 0.7)',  // light green
          'rgba(245, 182, 153, 0.7)',  // light orange
          'rgba(210, 157, 220, 0.7)',  // light purple
          'rgba(136, 198, 190, 0.7)',  // light cyan
          'rgba(242, 196, 160, 0.7)',  // light yellow
          'rgba(183, 183, 224, 0.7)',  // light blue-purple
          'rgba(225, 173, 173, 0.7)',  // light red
          'rgba(192, 205, 146, 0.7)',  // light yellow-green
          'rgba(210, 180, 140, 0.7)'   // light brown
        ]

        const option = {
          title: {
            text: 'Node Type Distribution (Bar Chart)',
            left: 'center'
          },
          tooltip: {
            trigger: 'axis'
          },
          xAxis: {
            type: 'category',
            data: labels,
            axisLabel: {
              rotate: 30
            }
          },
          yAxis: {
            type: 'value'
          },
          series: [{
            name: 'Count',
            type: 'bar',
            data: counts,
            itemStyle: {
              // Assign different colors to each bar
              color: (params) => {
                return colors[params.dataIndex % colors.length]
              },
              borderColor: 'rgba(255, 255, 255, 0.8)',
              borderWidth: 1
            }
          }]
        }

        this.barChartInstance.setOption(option)
      })
    },
    renderPieChart() {
      this.$nextTick(() => {
        if (!this.pieChartInstance) {
          this.pieChartInstance = echarts.init(document.getElementById('pie-chart'))
        }

        // Prepare pie chart data
        const pieData = this.nodeData.map(item => {
          return {
            name: item.label,
            value: item.count
          }
        })

        // Pie chart color array
        const pieColors = [
          '#66b8de', '#8fcd89', '#f5b699', '#d29ddc',
          '#88c6be', '#f2c4a0', '#b7b7e0', '#e1adad',
          '#c0cd92', '#d2b48c'
        ]

        const option = {
          title: {
            text: 'Node Type Distribution (Pie Chart)',
            left: 'center'
          },
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          },
          legend: {
            orient: 'horizontal',
            bottom: 10,
            data: this.nodeData.map(item => item.label)
          },
          series: [
            {
              name: 'Node Count',
              type: 'pie',
              radius: ['40%', '70%'], // ring pie chart
              avoidLabelOverlap: false,
              itemStyle: {
                borderRadius: 10,
                borderColor: '#fff',
                borderWidth: 2
              },
              label: {
                show: false,
                position: 'center'
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: '14',
                  fontWeight: 'bold'
                }
              },
              labelLine: {
                show: false
              },
              data: pieData,
              color: pieColors
            }
          ]
        }

        this.pieChartInstance.setOption(option)
      })
    },
    renderCategoryCharts() {
      // Build tree data
      const treeData = {
        name: 'Herb Classification',
        children: this.categoryData.map(item => ({
          name: item.level1,
          value: item.category2Count,
          children: item.children.map(child => ({
            name: child.level2,
            value: child.herbCount
          }))
        }))
      };

      // Level1 distribution data (including children info)
      const level1Data = this.categoryData.map(item => ({
        name: item.level1,
        value: item.category2Count,
        children: item.children
      }));

      // Tree chart (adjusted height and label display)
      this.$nextTick(() => {
        if (!this.categoryTreeChartInstance) {
          this.categoryTreeChartInstance = echarts.init(document.getElementById('category-tree-chart'))
        }

        const option = {
          title: {
            text: 'Herb Classification Hierarchy',
            left: 'center'
          },
          tooltip: {
            trigger: 'item',
            triggerOn: 'mousemove',
            formatter: (params) => {
              return `${params.name}: ${params.value || 0} herbs`
            }
          },
          series: [
            {
              type: 'tree',
              data: [treeData],
              top: '5%',
              left: '10%',
              bottom: '5%',
              right: '10%',
              symbolSize: 10,
              orient: 'LR',
              label: {
                position: 'left',
                verticalAlign: 'middle',
                align: 'right',
                fontSize: 14,
                color: '#333',
                overflow: 'break',
                width: 120
              },
              leaves: {
                label: {
                  position: 'right',
                  verticalAlign: 'middle',
                  align: 'left',
                  fontSize: 10,
                  color: '#333',
                  overflow: 'break',
                  width: 120
                }
              },
              emphasis: {
                focus: 'descendant'
              },
              expandAndCollapse: true,
              animationDuration: 550,
              animationDurationUpdate: 750,
              roam: true
            }
          ]
        };

        this.categoryTreeChartInstance.setOption(option)
      });

      // Level1 pie chart (increased height)
      this.$nextTick(() => {
        if (!this.level1PieChartInstance) {
          this.level1PieChartInstance = echarts.init(document.getElementById('level1-pie-chart'))
        }

        const option = {
          title: {
            text: 'Level 1 Classification Distribution',
            left: 'center'
          },
          tooltip: {
            trigger: 'item',
            formatter: (params) => {
              const data = params.data;
              // Show level1 name and level2 details
              let tooltipContent = `${data.name}<br/>Number of level2 categories: ${data.value}<br/><br/>Level2 details:`;
              if (data.children && data.children.length > 0) {
                data.children.forEach(child => {
                  tooltipContent += `<br/>${child.level2}: ${child.herbCount} herbs`;
                });
              }
              return tooltipContent;
            }
          },
          legend: {
            type: 'scroll',
            orient: 'horizontal',
            bottom: 10
          },
          series: [
            {
              name: 'Level 1 Categories',
              type: 'pie',
              radius: ['40%', '70%'],
              avoidLabelOverlap: false,
              itemStyle: {
                borderRadius: 10,
                borderColor: '#fff',
                borderWidth: 2
              },
              label: {
                show: false,
                position: 'center'
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: '14',
                  fontWeight: 'bold'
                }
              },
              labelLine: {
                show: false
              },
              data: level1Data,
              color: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452']
            }
          ]
        };

        this.level1PieChartInstance.setOption(option)
      });

      // Level2 herb count bar chart
      this.$nextTick(() => {
        if (!this.level2HerbCountChartInstance) {
          this.level2HerbCountChartInstance = echarts.init(document.getElementById('level2-herb-count-chart'))
        }

        // Level2 herb count data (top 15)
        const level2Data = [];
        this.categoryData.forEach(item => {
          item.children.forEach(child => {
            level2Data.push({
              name: child.level2,
              value: child.herbCount
            });
          });
        });

        // Sort by herb count descending
        level2Data.sort((a, b) => b.value - a.value);
        // Take top 15
        const topLevel2Data = level2Data.slice(0, 15);

        const option = {
          title: {
            text: 'Level 2 Classification Herb Count',
            left: 'center'
          },
          tooltip: {
            trigger: 'axis'
          },
          xAxis: {
            type: 'value'
          },
          yAxis: {
            type: 'category',
            data: topLevel2Data.map(item => item.name)
          },
          series: [{
            name: 'Herb Count',
            type: 'bar',
            data: topLevel2Data.map(item => item.value),
            itemStyle: {
              color: '#91cc75'
            }
          }]
        };

        this.level2HerbCountChartInstance.setOption(option)
      });
    },
    renderPropertyCharts() {
      // Process property data
      const propertyData = this.herbPropertiesData
          .filter(item => item.type === 'property' && item.value)
          .map(item => ({
            name: item.value,
            value: item.count
          }))
          .sort((a, b) => b.value - a.value);

      const propertyBarData = propertyData.slice(0, 8); // Take top 8 for bar chart

      // Property pie chart
      this.$nextTick(() => {
        if (!this.propertyPieChartInstance) {
          this.propertyPieChartInstance = echarts.init(document.getElementById('property-pie-chart'))
        }

        const option = {
          title: {
            text: 'Property Distribution (Pie Chart)',
            left: 'center'
          },
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          },
          legend: {
            type: 'scroll',
            orient: 'horizontal',
            bottom: 10
          },
          series: [
            {
              name: 'Property',
              type: 'pie',
              radius: ['40%', '70%'],
              avoidLabelOverlap: false,
              itemStyle: {
                borderRadius: 10,
                borderColor: '#fff',
                borderWidth: 2
              },
              label: {
                show: false,
                position: 'center'
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: '14',
                  fontWeight: 'bold'
                }
              },
              labelLine: {
                show: false
              },
              data: propertyData,
              color: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc']
            }
          ]
        }

        this.propertyPieChartInstance.setOption(option)
      });

      // Property bar chart
      this.$nextTick(() => {
        if (!this.propertyBarChartInstance) {
          this.propertyBarChartInstance = echarts.init(document.getElementById('property-bar-chart'))
        }

        const option = {
          title: {
            text: 'Property Distribution (Bar Chart)',
            left: 'center'
          },
          tooltip: {
            trigger: 'axis'
          },
          xAxis: {
            type: 'category',
            data: propertyBarData.map(item => item.name),
            axisLabel: {
              rotate: 30
            }
          },
          yAxis: {
            type: 'value'
          },
          series: [{
            name: 'Count',
            type: 'bar',
            data: propertyBarData.map(item => item.value),
            itemStyle: {
              color: '#5470c6'
            }
          }]
        }

        this.propertyBarChartInstance.setOption(option)
      });
    },
    renderTasteCharts() {
      // Process taste data
      const tasteData = this.herbPropertiesData
          .filter(item => item.type === 'taste' && item.value)
          .map(item => ({
            name: item.value,
            value: item.count
          }))
          .sort((a, b) => b.value - a.value);

      // Taste rose chart
      this.$nextTick(() => {
        if (!this.tasteRoseChartInstance) {
          this.tasteRoseChartInstance = echarts.init(document.getElementById('taste-rose-chart'))
        }

        const option = {
          title: {
            text: 'Taste Distribution (Rose Chart)',
            left: 'center'
          },
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          },
          legend: {
            type: 'scroll',
            orient: 'horizontal',
            bottom: 10
          },
          series: [
            {
              name: 'Taste',
              type: 'pie',
              radius: ['20%', '70%'],
              roseType: 'radius',
              itemStyle: {
                borderRadius: 5,
                borderColor: '#fff',
                borderWidth: 2
              },
              label: {
                show: false
              },
              emphasis: {
                label: {
                  show: true
                }
              },
              labelLine: {
                show: false
              },
              data: tasteData,
              color: ['#8fcd89', '#f5b699', '#d29ddc', '#88c6be', '#f2c4a0', '#b7b7e0', '#e1adad', '#c0cd92', '#d2b48c']
            }
          ]
        }

        this.tasteRoseChartInstance.setOption(option)
      });

      // Taste bar chart
      this.$nextTick(() => {
        if (!this.tasteBarChartInstance) {
          this.tasteBarChartInstance = echarts.init(document.getElementById('taste-bar-chart'))
        }

        const barData = tasteData.slice(0, 8); // Take top 8 for bar chart

        const option = {
          title: {
            text: 'Taste Distribution (Bar Chart)',
            left: 'center'
          },
          tooltip: {
            trigger: 'axis'
          },
          xAxis: {
            type: 'value'
          },
          yAxis: {
            type: 'category',
            data: barData.map(item => item.name)
          },
          series: [{
            name: 'Count',
            type: 'bar',
            data: barData.map(item => item.value),
            itemStyle: {
              color: '#91cc75'
            }
          }]
        }

        this.tasteBarChartInstance.setOption(option)
      });
    },
    renderChannelCharts() {
      // Process channel tropism data
      const channelData = this.herbPropertiesData
          .filter(item => item.type === 'channel_tropism' && item.value)
          .map(item => ({
            name: item.value,
            value: item.count
          }))
          .sort((a, b) => b.value - a.value);

      // Channel funnel chart
      this.$nextTick(() => {
        if (!this.channelFunnelChartInstance) {
          this.channelFunnelChartInstance = echarts.init(document.getElementById('channel-funnel-chart'))
        }

        const funnelData = channelData.slice(0, 8); // Take top 8 for funnel chart

        const option = {
          title: {
            text: 'Channel Tropism Distribution (Funnel Chart)',
            left: 'center'
          },
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          },
          legend: {
            orient: 'horizontal',
            bottom: 10
          },
          series: [
            {
              name: 'Channel Tropism',
              type: 'funnel',
              left: '10%',
              top: 60,
              bottom: 60,
              width: '80%',
              min: 0,
              max: 100,
              minSize: '0%',
              maxSize: '100%',
              sort: 'descending',
              gap: 2,
              label: {
                show: true,
                position: 'inside'
              },
              labelLine: {
                length: 10,
                lineStyle: {
                  width: 1,
                  type: 'solid'
                }
              },
              itemStyle: {
                borderColor: '#fff',
                borderWidth: 1
              },
              emphasis: {
                label: {
                  fontSize: 14
                }
              },
              data: funnelData.map((item, index) => ({
                name: item.name,
                value: item.value,
                itemStyle: {
                  color: `rgb(${Math.floor(200 - index * 15)}, ${Math.floor(100 + index * 10)}, ${Math.floor(200 - index * 10)})`
                }
              }))
            }
          ]
        }

        this.channelFunnelChartInstance.setOption(option)
      });

      // Channel radar chart
      this.$nextTick(() => {
        if (!this.channelRadarChartInstance) {
          this.channelRadarChartInstance = echarts.init(document.getElementById('channel-radar-chart'))
        }

        const radarData = channelData.slice(0, 6); // Take top 6 for radar chart

        const option = {
          title: {
            text: 'Channel Tropism Distribution (Radar Chart)',
            left: 'center'
          },
          tooltip: {
            trigger: 'item'
          },
          radar: {
            indicator: radarData.map(item => ({
              name: item.name,
              max: Math.max(...radarData.map(d => d.value))
            })),
            radius: '60%'
          },
          series: [{
            name: 'Channel Tropism',
            type: 'radar',
            areaStyle: {
              opacity: 0.7
            },
            itemStyle: {
              color: '#fac858'
            },
            data: [
              {
                value: radarData.map(item => item.value),
                name: 'Channel Tropism Count'
              }
            ]
          }]
        }

        this.channelRadarChartInstance.setOption(option)
      });
    }
  }
}
</script>

<style scoped>
.graph-stats-container {
  padding: 20px;
}

.title {
  font-size: 22px;
  font-weight: bold;
  color: #2a6f97;
}

/* Chart container using flex layout */
.chart-container {
  display: flex;
  justify-content: space-between;
  gap: 20px;
}

/* Each chart item takes half width */
.chart-item {
  width: 50%;
  height: 400px;
}

.chart-section {
  margin-bottom: 30px;
}

.chart-section:last-child {
  margin-bottom: 0;
}
</style>