<template>
  <div class="graph-search-container">
    <el-card>
      <template #header>
        <span class="title">Graph Search (ECharts)</span>
      </template>

      <div class="search-controls">
        <el-select v-model="selectedType" placeholder="Select node type" style="width: 200px; margin-right: 20px">
          <el-option v-for="item in nodeTypes" :key="item" :label="item" :value="item" />
        </el-select>

        <el-input v-model="nodeName" placeholder="Enter node name" style="width: 300px; margin-right: 20px" />
        <el-button type="primary" @click="search">Search</el-button>
      </div>

      <div v-if="searched && !chartData.nodes.length" class="no-data">No data available</div>
      <div class="chart-wrapper" v-show="chartData.nodes.length" @click="hideContextMenu">
        <div ref="chartRef" class="echarts-container"></div>
        <!-- Legend panel -->
        <div class="legend-panel" :class="{ 'collapsed': !legendPanelVisible }">
          <div class="legend-header">
            <div class="legend-title">Filter by Node Type</div>
            <div class="legend-toggle" @click="toggleAllNodeTypes">
              {{ visibleNodeTypes.length === nodeTypes.length ? 'Deselect All' : 'Select All' }}
            </div>
          </div>
          <div class="legend-items" v-show="legendPanelVisible">
            <div
                v-for="type in nodeTypes"
                :key="type"
                class="legend-item"
                :class="{ active: visibleNodeTypes.includes(type) }"
                @click="toggleNodeType(type)"
            >
              <div class="legend-color" :style="{ backgroundColor: colorMap[type] }"></div>
              <div class="legend-label">{{ type }}</div>
              <div class="legend-checkbox">
                <i :class="visibleNodeTypes.includes(type) ? 'el-icon-check' : ''"></i>
              </div>
            </div>
          </div>
          <!-- Collapse/Expand button -->
          <div class="legend-collapse" @click="toggleLegendPanel">
            <i :class="legendPanelVisible ? 'el-icon-arrow-down' : 'el-icon-arrow-up'"></i>
          </div>
        </div>

        <!-- Context menu -->
        <div
            v-show="contextMenuVisible"
            class="context-menu"
            :style="{ left: contextMenuPosition.x + 'px', top: contextMenuPosition.y + 'px' }"
        >
          <div class="context-menu-item" @click.stop="expandNode">Expand Related Info</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import request from '@/utils/request'

export default {
  data() {
    return {
      nodeTypes: ['Herb', 'Protein', 'Ingredients', 'Disease', 'Pathway', 'MedicineLevel1Category', 'MedicineLevel2Category'],
      selectedType: 'Herb',
      nodeName: '葛根',
      chartData: { nodes: [], links: [] },
      searched: false,
      legendPanelVisible: true,
      visibleNodeTypes: ['Herb', 'Protein', 'Ingredients', 'Disease', 'Pathway', 'MedicineLevel1Category', 'MedicineLevel2Category'],
      colorMap: {
        Herb: '#2a6f97',
        Ingredients: '#38b000',
        Protein: '#ef476f',
        Disease: '#f3722c',
        Pathway: '#3a0ca3',
        MedicineLevel1Category: '#4361ee',
        MedicineLevel2Category: '#4cc9f0'
      },
      contextMenuVisible: false,
      contextMenuPosition: { x: 0, y: 0 },
      rightClickedNode: null,
      expandedNodeIds: new Set(),
      chartInstance: null
    }
  },

  computed: {
    displayData() {
      const visibleNodes = this.chartData.nodes.filter(n => this.visibleNodeTypes.includes(n.category))
      const visibleIds = new Set(visibleNodes.map(n => n.id))
      const visibleLinks = this.chartData.links.filter(l => visibleIds.has(l.source) && visibleIds.has(l.target))
      return { nodes: visibleNodes, links: visibleLinks }
    }
  },

  mounted() {
    // Non-reactive: used for canonical ID mapping (do not put in data)
    this._nodeKey2Id = new Map() // key => canonicalId (key itself)
    this.chartInstance = echarts.init(this.$refs.chartRef)
    window.addEventListener('resize', this.handleResize)
    document.addEventListener('click', this.hideContextMenu)
    this.$refs.chartRef.addEventListener('contextmenu', e => e.preventDefault())
    this.renderChart()
  },

  beforeDestroy() {
    if (this.chartInstance) this.chartInstance.dispose()
    window.removeEventListener('resize', this.handleResize)
    document.removeEventListener('click', this.hideContextMenu)
  },

  methods: {
    /* ---------------- Normalization and mapping ---------------- */
    keyFromDataObj(data = {}) {
      const label = data.label || data.category || ''
      const name = String(data.name || data.id || '').trim()
      return `${label}::${name}`
    },
    ensureCanonicalIdForNodeData(data = {}) {
      const key = this.keyFromDataObj(data)
      if (!this._nodeKey2Id.has(key)) {
        this._nodeKey2Id.set(key, key) // canonical id is the key itself
      }
      return this._nodeKey2Id.get(key)
    },
    // Generate a node reference from a graph node object for backend
    makeRefFromGraphNode(n) {
      if (n.category === 'Protein') {
        // Prefer original data value.id (Uniprot ID)
        const uniprot = n.value?.id ?? n.id
        return { label: 'Protein', uniprot_id: String(uniprot) }
      }
      const nm = n.value?.name ?? n.name ?? n.id
      return { label: n.category, name: String(nm) }
    },

    // Build search payload (selectedType + user input)
    makeSearchPayload(label, userInput) {
      if (label === 'Protein') {
        return { label: 'Protein', uniprot_id: String(userInput) }
      }
      return { label, name: String(userInput) }
    },

    /* ---------------- Legend interactions ---------------- */
    toggleNodeType(type) {
      const i = this.visibleNodeTypes.indexOf(type)
      if (i > -1) this.visibleNodeTypes.splice(i, 1)
      else this.visibleNodeTypes.push(type)
      this.renderChart()
    },
    toggleAllNodeTypes() {
      if (this.visibleNodeTypes.length === this.nodeTypes.length) {
        this.visibleNodeTypes = []
      } else {
        this.visibleNodeTypes = [...this.nodeTypes]
      }
      this.renderChart()
    },
    toggleLegendPanel() {
      this.legendPanelVisible = !this.legendPanelVisible
    },

    /* ---------------- Context menu ---------------- */
    showContextMenu(params, event) {
      this.contextMenuVisible = true
      const rect = this.$refs.chartRef.getBoundingClientRect()
      const x = event.offsetX + 10
      const y = event.offsetY + 10
      this.contextMenuPosition = {
        x: Math.min(x, rect.width - 160),
        y: Math.min(y, rect.height - 40)
      }
      this.rightClickedNode = params.data // {id, name, category, value: original data}
    },
    hideContextMenu() {
      this.contextMenuVisible = false
      this.rightClickedNode = null
    },

    /* ---------------- Initial search: replace entire graph ---------------- */
    replaceGraphWith(data) {
      // Clear old graph and mapping
      this.chartData = { nodes: [], links: [] }
      this._nodeKey2Id = new Map()

      // Temporary mapping within this batch: rawId -> canonicalId
      const batchMap = new Map()
      const nodes = []
      const links = []
      const linkKeySet = new Set()

      // Normalize nodes
      for (const n of (data.nodes || [])) {
        const canonicalId = this.ensureCanonicalIdForNodeData(n.data)
        batchMap.set(n.data.id, canonicalId)
        nodes.push({
          id: canonicalId,
          name: n.data.name || n.data.id,
          category: n.data.label,
          symbolSize: 40,
          value: n.data
        })
      }

      // Normalize edges (undirected, deduplicate)
      for (const e of (data.edges || [])) {
        const s = batchMap.get(e.data.source)
        const t = batchMap.get(e.data.target)
        if (!s || !t || s === t) continue
        const key = [s, t].sort().join('|')
        if (linkKeySet.has(key)) continue
        linkKeySet.add(key)
        links.push({
          source: s,
          target: t,
          label: { show: true, formatter: e.data.label }
        })
      }

      this.chartData = { nodes, links }
    },

    /* ---------------- Expand: merge new data with existing graph ---------------- */
    mergeNewData(newData, mainCanonicalId, mainKey) {
      const existingNodeIds = new Set(this.chartData.nodes.map(n => n.id))
      const existingLinkKeys = new Set(this.chartData.links.map(l => [l.source, l.target].sort().join('|')))

      const batchMap = new Map()
      const newNodes = []
      const newLinks = []

      // Normalize new nodes
      for (const n of (newData.nodes || [])) {
        const canonicalId = this.ensureCanonicalIdForNodeData(n.data)
        batchMap.set(n.data.id, canonicalId)
        if (!existingNodeIds.has(canonicalId)) {
          newNodes.push({
            id: canonicalId,
            name: n.data.name || n.data.id,
            category: n.data.label,
            symbolSize: 40,
            value: n.data
          })
          existingNodeIds.add(canonicalId)
        }
      }

      // Normalize new edges; if an endpoint is an alias of the main node, map to mainCanonicalId
      const mapEndpoint = (rawId) => {
        const canonical = batchMap.get(rawId)
        if (!canonical) return null
        // If this canonical equals the main node's key in this batch, use mainCanonicalId
        return (canonical === mainKey) ? mainCanonicalId : canonical
      }

      for (const e of (newData.edges || [])) {
        let s = mapEndpoint(e.data.source)
        let t = mapEndpoint(e.data.target)
        if (!s || !t || s === t) continue
        const key = [s, t].sort().join('|')
        if (existingLinkKeys.has(key)) continue
        existingLinkKeys.add(key)
        newLinks.push({
          source: s,
          target: t,
          label: { show: true, formatter: e.data.label }
        })
      }

      // Fallback: if backend didn't explicitly connect main node to new nodes, add a link
      const connectedToMain = new Set(
          this.chartData.links
              .concat(newLinks)
              .flatMap(l => (l.source === mainCanonicalId ? [l.target] : (l.target === mainCanonicalId ? [l.source] : [])))
      )
      for (const n of newNodes) {
        if (n.id === mainCanonicalId) continue
        if (!connectedToMain.has(n.id)) {
          const key = [mainCanonicalId, n.id].sort().join('|')
          if (!existingLinkKeys.has(key)) {
            existingLinkKeys.add(key)
            newLinks.push({ source: mainCanonicalId, target: n.id, label: { show: false } })
          }
        }
      }

      this.chartData.nodes.push(...newNodes)
      this.chartData.links.push(...newLinks)
    },

    /* ---------------- API calls: search & expand ---------------- */
    async search() {
      try {
        this.chartData = { nodes: [], links: [] }

        // Protein uses uniprot_id, other types use name
        const payload = this.makeSearchPayload(this.selectedType, this.nodeName)

        const res = await request.post('/api/graph/search', payload)
        this.searched = true
        if (res.code === '200') {
          this.replaceGraphWith(res.data)
          this.$nextTick(() => this.renderChart())
        } else {
          this.$message.error(res.msg || 'Search failed')
        }
      } catch (err) {
        this.searched = true
        this.$message.error('Request failed')
      }
    },

    async expandNode() {
      const mainNode = this.rightClickedNode
      if (!mainNode || !mainNode.id) {
        this.$message.warning('No node selected or invalid node data')
        return
      }
      this.hideContextMenu()

      const mainCanonicalId = mainNode.id
      const mainKey = this.keyFromDataObj(mainNode.value || {})

      if (this.expandedNodeIds.has(mainCanonicalId)) {
        this.$message.info(`Node "${mainNode.name || mainCanonicalId}" has already been expanded`)
        return
      }

      try {
        // Already related neighbors → related
        const relatedLinks = this.chartData.links.filter(l => l.source === mainCanonicalId || l.target === mainCanonicalId)
        const relatedIds = new Set(); relatedLinks.forEach(l => { relatedIds.add(l.source); relatedIds.add(l.target) })
        relatedIds.delete(mainCanonicalId)
        const relatedNodes = this.chartData.nodes.filter(n => relatedIds.has(n.id))

        // Main node payload: Protein uses uniprot_id, others use name (from original value)
        let mainPayload
        if (mainNode.category === 'Protein') {
          const uniprot = mainNode.value?.uniprot_id ?? mainCanonicalId
          mainPayload = { label: 'Protein', name: String(uniprot) }
        } else {
          const name = mainNode.value?.name ?? mainNode.name ?? mainCanonicalId
          mainPayload = { label: mainNode.category, name: String(name) }
        }

        // related also follows the type-specific format (Protein uses uniprot_id)
        const payload = {
          ...mainPayload,
          related: relatedNodes.map(n => this.makeRefFromGraphNode(n))
        }

        const res = await request.post('/api/graph/search/expand', payload)
        if (res.code === '200') {
          this.mergeNewData(res.data, mainCanonicalId, mainKey)
          this.expandedNodeIds.add(mainCanonicalId)
          this.$nextTick(() => this.renderChart())
          this.$message.success(`Expanded "${mainNode.name || mainCanonicalId}" successfully`)
        } else {
          this.$message.error(res.msg || 'Expansion failed')
        }
      } catch (err) {
        this.$message.error('Request failed')
      }
    },

    /* ---------------- Chart rendering ---------------- */
    formatNodeTooltip(nodeData) {
      let t = `<div style='max-width:400px;'>`
      t += `<strong>${nodeData.name || nodeData.id}</strong><br>`
      t += `Type: ${nodeData.label}<br>`
      for (const k in nodeData) {
        if (['id', 'name', 'label'].includes(k)) continue
        const v = nodeData[k]
        if (Array.isArray(v) && v.length) t += `${k}: ${v.join(', ')}<br>`
        else if (typeof v === 'string' && v.trim()) t += `${k}: ${v.length > 100 ? v.substring(0, 100) + '...' : v}<br>`
        else if (v !== null && v !== undefined) t += `${k}: ${v}<br>`
      }
      t += `</div>`
      return t
    },

    renderChart() {
  if (!this.chartInstance) {
    this.chartInstance = echarts.init(this.$refs.chartRef)
  }
  this.chartInstance.resize()
  this.chartInstance.setOption({
    tooltip: {
      backgroundColor: 'rgba(255,255,255,0.9)',
      borderColor: '#ccc',
      borderWidth: 1,
      padding: 10,
      textStyle: { color: '#333', fontSize: 12 },
      formatter: (p) => {
        if (p.data?.value) return this.formatNodeTooltip(p.data.value)
        if (p.data?.label) return `Relation: ${p.data.label.formatter}`
      }
    },
    series: [{
      type: 'graph',
      layout: 'force',
      data: this.displayData.nodes,
      links: this.displayData.links,
      categories: this.nodeTypes.map(t => ({ name: t, itemStyle: { color: this.colorMap[t] } })),
      roam: true,
      label: { show: true, position: 'right' },
      lineStyle: { color: '#999', curveness: 0.2 },
      force: {
        repulsion: 1200,
        edgeLength: [150, 400],
        gravity: 0.1,
        friction: 0.9,
        layoutAnimation: true,
        layoutIterations: 30,
        nodeScaleRatio: 0.4
      },
      emphasis: { focus: 'none' },  // 修改这里：取消节点点击后的虚化效果
      itemStyle: { color: (p) => this.colorMap[p.data.category] || '#888' },
      draggable: true
    }]
  })

  // 事件绑定保持不变
  this.chartInstance.off('click')
  this.chartInstance.on('click', () => this.hideContextMenu())

  this.chartInstance.off('contextmenu')
  this.chartInstance.on('contextmenu', (params) => {
    if (params.dataType === 'node') {
      this.showContextMenu(params, params.event.event)
    }
  })
}
  }
}
</script>

<style scoped>
.graph-search-container {
  padding: 20px;
}

.search-controls {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.chart-wrapper {
  position: relative;
  width: 100%;
  height: 900px;
  margin-top: 20px;
}

.echarts-container {
  width: 100%;
  height: 100%;
  background: #ffffff;
  min-width: 800px;
}

.title {
  font-size: 20px;
  font-weight: bold;
  color: #2a6f97;
}

.no-data {
  text-align: center;
  margin-top: 30px;
  color: #888;
}

/* Legend panel styles */
.legend-panel {
  position: absolute;
  top: 20px;
  right: 20px;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 6px 24px 0 rgba(0, 0, 0, 0.2);
  z-index: 1000;
  width: 300px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.legend-panel.collapsed {
  width: 50px;
  padding: 10px;
}

.legend-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.legend-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.legend-toggle {
  font-size: 12px;
  color: #409eff;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.legend-toggle:hover {
  background-color: #f0f8ff;
}

.legend-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.legend-item {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 10px 12px;
  border-radius: 8px;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.legend-item:hover {
  background-color: #f5f7fa;
  border-color: #dcdfe6;
}

.legend-item.active {
  background-color: #ecf5ff;
  border-color: #b3d8ff;
}

.legend-color {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  margin-right: 12px;
  border: 2px solid #fff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.legend-label {
  flex: 1;
  font-size: 14px;
  color: #606266;
}

.legend-checkbox {
  width: 18px;
  height: 18px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
  font-weight: bold;
}

.legend-item.active .legend-checkbox {
  background-color: #409eff;
  border-color: #409eff;
  color: white;
}

.legend-collapse {
  position: absolute;
  bottom: -20px;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 20px;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 0 0 12px 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(0, 0, 0, 0.05);
  border-top: none;
}

.legend-panel.collapsed .legend-collapse {
  border-radius: 12px;
  bottom: -10px;
}

/* Context menu styles */
.context-menu {
  position: absolute;
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 2000;
  min-width: 150px;
  border: 1px solid #ebeef5;
}

.context-menu-item {
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
}

.context-menu-item:hover {
  background-color: #ecf5ff;
  color: #409eff;
}
</style>