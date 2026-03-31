<template>
  <div class="herb-detail-container">
    <el-card v-loading="loading" element-loading-text="Loading ingredient details...">
      <div slot="header" class="card-header">
        <span class="card-title">Ingredient Details</span>
        <el-button size="small" @click="$router.back()">Back</el-button>
      </div>

      <!-- Basic Info -->
      <div class="section">
        <div class="section-title basic-info">
          <span class="icon">📋</span>
          <span class="text">Basic Information</span>
        </div>
        <el-row :gutter="20">
          <el-col :span="12"><p><strong>ID：</strong>{{ detail.id }}</p></el-col>
          <el-col :span="12"><p><strong>Name：</strong>{{ detail.name }}</p></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><p><strong>Alias：</strong>{{ detail.alias }}</p></el-col>
          <el-col :span="12"><p><strong>Molecular Weight：</strong>{{ detail.ingredientWeight }}</p></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><p><strong>TCMSP ID：</strong>{{ detail.tcmspId }}</p></el-col>
          <el-col :span="12"><p><strong>PubChem ID：</strong>{{ detail.pubChemId }}</p></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><p><strong>PubChem CID：</strong>{{ detail.pubChemCID }}</p></el-col>
          <el-col :span="12">
            <p><strong>PubChem Link：</strong>
              <el-link v-if="detail.pubChemCIDLink" :href="detail.pubChemCIDLink" target="_blank">
                {{ detail.pubChemCIDLink }}
              </el-link>
              <span v-else>--</span>
            </p>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><p><strong>SMILES：</strong>{{ detail.smiles || '--' }}</p></el-col>
          <el-col :span="12"><p><strong>InChIKey：</strong>{{ detail.inChIKey || '--' }}</p></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><p><strong>Formula：</strong>{{ detail.formula || '--' }}</p></el-col>
          <el-col :span="12"><p><strong>OB Score：</strong>{{ detail.obScore || '--' }}</p></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><p><strong>HUBU ID：</strong>{{ detail.hubuId }}</p></el-col>
          <el-col :span="12"><p><strong>SymMap ID：</strong>{{ detail.symMapId || '--' }}</p></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24"><p><strong>CAS：</strong>{{ (detail.cas || []).join('、') || '--' }}</p></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <p><strong>Pharmacological & Molecular Properties：</strong>{{ detail.pharmacologicalAndMolecularPropertiesData || 'No data' }}</p>
          </el-col>
        </el-row>
      </div>

      <!-- Related Proteins -->
      <div v-if="detail.proteins?.length" class="section">
        <div class="section-title protein">
          <span class="icon">🧬</span>
          <span class="text">Related Proteins</span>
        </div>
        <el-table :data="pagedProteins" style="width: 100%">
          <el-table-column label="ID">
            <template slot-scope="scope">
              <el-link :underline="false" type="primary" @click="goToProtein(scope.row.id)">
                {{ scope.row.id }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column label="Protein Name" prop="name" />
        </el-table>
        <div class="pagination-wrapper">
          <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="detail.proteins.length"
              :page-size="pageSizeProtein"
              :current-page.sync="pageProtein"
              :page-sizes="[5, 10, 20, 50]"
              @size-change="val => pageSizeProtein = val"
              @current-change="val => pageProtein = val"
          />
        </div>
      </div>

      <!-- Related Herbs -->
      <div v-if="detail.herbs?.length" class="section">
        <div class="section-title herb">
          <span class="icon">🌿</span>
          <span class="text">Related Herbs</span>
        </div>
        <el-table :data="pagedHerbs" style="width: 100%">
          <el-table-column label="ID">
            <template slot-scope="scope">
              <el-link :underline="false" type="success" @click="goToHerb(scope.row.id)">
                {{ scope.row.id }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column label="Herb Name" prop="name" />
        </el-table>
        <div class="pagination-wrapper">
          <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="detail.herbs.length"
              :page-size="pageSizeHerb"
              :current-page.sync="pageHerb"
              :page-sizes="[5, 10, 20, 50]"
              @size-change="val => pageSizeHerb = val"
              @current-change="val => pageHerb = val"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'IngredientDetail',
  data() {
    return {
      detail: {},
      loading: false,
      pageProtein: 1,
      pageSizeProtein: 5,
      pageHerb: 1,
      pageSizeHerb: 5
    }
  },
  computed: {
    pagedProteins() {
      const start = (this.pageProtein - 1) * this.pageSizeProtein
      return (this.detail.proteins || []).slice(start, start + this.pageSizeProtein)
    },
    pagedHerbs() {
      const start = (this.pageHerb - 1) * this.pageSizeHerb
      return (this.detail.herbs || []).slice(start, start + this.pageSizeHerb)
    }
  },
  created() {
    const id = this.$route.params.id
    if (!id || id === 'undefined') {
      this.$message.error('Invalid ingredient ID')
      return
    }
    this.loadDetail(id)
  },
  methods: {
    loadDetail(id) {
      this.loading = true
      request.get(`/api/ingredient/detail/${id}`)
          .then(res => {
            if (res.code === '200') {
              this.detail = res.data.ingredientsDTO || {}
              this.detail.proteins = res.data.proteins || []
              this.detail.herbs = res.data.herbs || []
            } else {
              this.$message.error(res.msg || 'Failed to get ingredient details')
            }
          })
          .catch(err => {
            console.error('Load error:', err)
            this.$message.error('Request failed')
          })
          .finally(() => {
            this.loading = false
          })
    },
    goToProtein(id) {
      this.$router.push(`/protein/detail/${id}`)
    },
    goToHerb(id) {
      this.$router.push(`/herb/detail/${id}`)
    }
  }
}
</script>

<style scoped>
.herb-detail-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 24px;
  font-weight: bold;
  color: #2d6a4f;
}

.section {
  margin-bottom: 24px;
  padding: 0;
}

.section-title {
  display: flex;
  align-items: center;
  width: 100%;
  font-size: 20px;
  font-weight: bold;
  border-radius: 6px;
  padding: 10px 16px;
  margin-bottom: 16px;
}

.section-title .icon {
  font-size: 22px;
  margin-right: 8px;
}

.pagination-wrapper {
  text-align: center;
  margin-top: 20px;
}
</style>