<template>
  <div class="homepage-container">

    <!-- Hero Banner -->
    <div class="hero-banner">
      <img src="@/assets/css/img/tupian_bk2.png" alt="TCM Herb" class="hero-image" />
      <div class="hero-text">Welcome to TCMKP Platform</div>
    </div>

    <!-- Node Cards (flex ) -->
    <div class="card-flex-row">
      <router-link v-for="item in nodeData" :key="item.label" :to="item.path" class="card-link">
        <el-card shadow="hover" class="stat-card">
          <div class="card-emoji">{{ item.icon }}</div>
          <div class="title">{{ item.label }}</div>
          <div class="count">{{ item.count }}</div>
        </el-card>
      </router-link>
    </div>

    <!-- Feature Hub (colored functional cards) -->
    <el-card shadow="never" class="analysis-card">
      <div slot="header" class="analysis-header">
        Platform Features
      </div>
      <el-row :gutter="30" type="flex" justify="center" class="feature-row">
        <el-col :span="8" v-for="(item, index) in featureData" :key="item.label">
          <router-link :to="item.path" class="card-link">
            <el-card shadow="hover" class="feature-card" :style="{ backgroundColor: featureColors[index] }">
              <div class="card-emoji">
                <i :class="item.icon"></i>
              </div>
              <div class="card-link">{{ item.label }}</div>
              <div class="inner-desc">{{ item.desc }}</div>
            </el-card>
          </router-link>
        </el-col>
      </el-row>
    </el-card>

  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  data() {
    return {
      nodeData: [],
      featureData: [
        {
          label: 'Herb Overview Statistics',
          desc: 'Explore individual herbs: flavor, meridian, properties, and statistics',
          icon: 'el-icon-s-claim',
          path: '/graph/stats'
        },
        {
          label: 'Data Details',
          desc: 'Visualize herbs, ingredients, targets, and diseases relationships',
          icon: 'el-icon-menu',
          path: '/manager/herb'
        },
        {
          label: 'Knowledge Extraction',
          desc: 'Use AI to extract structured TCM knowledge from texts',
          icon: 'el-icon-data-analysis',
          path: '/manager/llm'
        },
        {
          label: 'Target Prediction',
          desc: 'Predict possible herb-target links using R-GCN',
          icon: 'el-icon-connection',
          path: '/manager/predictTargets'
        },
        {
          label: 'Multi-node Query',
          desc: 'Query across herbs, diseases, ingredients, and targets simultaneously',
          icon: 'el-icon-s-grid',
          path: '/searchAll'
        },
        {
          label: 'Graph Query',
          desc: 'Generate subgraphs for analysis based on selected nodes',
          icon: 'el-icon-search',
          path: '/graph/search'
        }
      ],
      featureColors: [
        '#FDE3A7', '#D6EAF8', '#FADBD8', '#D5F5E3', '#FCF3CF', '#E8DAEF'
      ]
    }
  },
  created() {
    this.loadChartData()
  },
  methods: {
    loadChartData() {
      request.get('/api/stats/node/total').then(res => {
        if (res && Array.isArray(res)) {
          this.nodeData = res.map(item => {
            const map = {
              Herb: { icon: '🌿', path: '/manager/herb' },
              Ingredients: { icon: '🧪', path: '/manager/ingredients' },
              Protein: { icon: '🩺', path: '/manager/protein' },
              Disease: { icon: '🩺', path: '/manager/disease' },
              Pathway: { icon: '🧭', path: '/manager/pathway' },
              MedicineLevel1Category: { icon: '📂', path: '/manager/medicineCategory' },
              MedicineLevel2Category: { icon: '📂', path: '/manager/medicineCategory' }
            }
            return {
              ...item,
              icon: map[item.label]?.icon || '🌿',
              path: map[item.label]?.path || '/manager/home'
            }
          })
        }
      })
    }
  }
}
</script>
<style scoped>
.homepage-container {
  padding: 40px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

/* Hero Banner */
.hero-banner {
  position: relative;
  text-align: center;
  margin-bottom: 40px;
}
.hero-image {
  width: 100%;
  max-height: 250px;
  object-fit: cover;
  border-radius: 12px;
}
.hero-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 32px;
  font-weight: bold;
  color: #fff;
  text-shadow: 2px 2px 8px rgba(0,0,0,0.5);
}

/* Node card container: Grid layout, 7 columns per row, equal width */
.card-flex-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 20px;
  margin-bottom: 40px;
  width: 100%;
  overflow-x: auto;
}

/* Scrollable with minimum width when screen is too small */
@media (max-width: 1200px) {
  .card-flex-row {
    overflow-x: auto;
    grid-template-columns: repeat(7, minmax(140px, 1fr));
  }
}

.stat-card {
  cursor: pointer;
  border-radius: 12px;
  transition: all 0.3s;
  text-align: center;
  padding: 12px 0;
  background-color: #fff;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 20px rgba(0,0,0,0.1);
}

.card-emoji {
  font-size: 32px;
  margin-bottom: 8px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  border-bottom: 2px solid #e0e0e0;
  padding-bottom: 6px;
  margin: 0 16px 6px 16px;
  width: auto;
}

.count {
  font-size: 28px;
  font-weight: bold;
  margin-top: 6px;
}

.card-link {
  display: block;
  text-decoration: none;
  color: #333;
}

/* Feature Hub Cards */
.feature-row {
  margin-top: 40px;
  flex-wrap: wrap;
  justify-content: center;
}
.feature-card {
  text-align: center;
  cursor: pointer;
  border-radius: 12px;
  padding: 20px 10px;
  margin-bottom: 20px;
  transition: all 0.3s;
}
.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 20px rgba(0,0,0,0.1);
}
.feature-card .card-link {
  display: block;
  text-decoration: underline;
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 6px;
}
.inner-desc {
  font-size: 13px;
  color: #333;
  line-height: 1.4;
  margin-top: 6px;
}
.analysis-header {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}
</style>