<template>
  <div class="ingredient-container">
    <el-card>
      <template #header>
        <div class="header-wrapper">
          <span class="title">Ingredient Information</span>

          <div class="title-search">
            <div class="search-bar">
              <el-input
                  v-model="searchKeyword"
                  placeholder="Enter ID, Name, CAS, Formula or SMILES"
                  size="small"
                  clearable
                  @keyup.enter.native="doSearch"
                  style="width: 300px; margin-right: 10px"
              />
              <el-button size="small" type="primary" @click="doSearch">Search</el-button>
              <el-button size="small" @click="resetSearch">Reset</el-button>
            </div>

            <div class="tip-container">
              <ul class="search-tip">
                <li>Ingredient ID or Name</li>
                <li>CAS Number or SMILES</li>
                <li>Example: ING000009, 523-42-2, Cyanin, CCCCCC=CCC=CCCCCCO</li>
              </ul>
            </div>
          </div>
        </div>
      </template>

      <el-table :data="pagedData" style="width: 100%">
        <el-table-column prop="id" label="ID" width="180">
          <template #default="scope">
            <router-link :to="`/ingredient/detail/${scope.row.id}`" class="link">{{ scope.row.id }}</router-link>
          </template>
        </el-table-column>

        <el-table-column prop="name" label="Name">
          <template #default="scope">
            <router-link :to="`/ingredient/detail/${scope.row.id}`" class="link">{{ scope.row.name }}</router-link>
          </template>
        </el-table-column>

        <el-table-column prop="cas" label="CAS Number" width="150">
          <template #default="scope">
            <span v-if="Array.isArray(scope.row.cas) && scope.row.cas.length > 0">
              {{ scope.row.cas.join(', ') }}
            </span>
            <span v-else-if="!Array.isArray(scope.row.cas) && scope.row.cas">
              {{ scope.row.cas }}
            </span>
            <span v-else>
              <i class="el-icon-info" style="color: #909399; font-size: 14px;"></i>
              <span style="color: #909399; margin-left: 4px;">No CAS</span>
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="smiles" label="SMILES" />
        <el-table-column prop="formula" label="Molecular Formula" />
      </el-table>

      <div style="text-align: center; margin-top: 20px">
        <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="filteredList.length"
            :page-size="pageSize"
            :current-page="currentPage"
            :page-sizes="[5, 10, 20, 50]"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'IngredientsList',
  data() {
    return {
      ingredients: [],
      filteredList: [],
      searchKeyword: '',
      currentPage: 1,
      pageSize: 10
    }
  },
  computed: {
    pagedData() {
      const start = (this.currentPage - 1) * this.pageSize
      return this.filteredList.slice(start, start + this.pageSize)
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    loadData() {
      request.get('api/ingredient/all').then(res => {
        if (res.code === '200') {
          this.ingredients = res.data
          this.filteredList = res.data
        } else {
          this.$message.error(res.msg || 'Failed to fetch ingredient information')
        }
      })
    },
    doSearch() {
      const keyword = this.searchKeyword.trim().toLowerCase()
      this.filteredList = this.ingredients.filter(item => {
        const idMatch = item.id?.toLowerCase().includes(keyword)
        const nameMatch = item.name?.toLowerCase().includes(keyword)
        const formulaMatch = item.formula?.toLowerCase().includes(keyword)
        const smilesMatch = item.smiles?.toLowerCase().includes(keyword)
        const casMatch = (item.cas || []).some(c => c.toLowerCase().includes(keyword))
        return idMatch || nameMatch || formulaMatch || smilesMatch || casMatch
      })
      this.currentPage = 1
    },
    resetSearch() {
      this.searchKeyword = ''
      this.filteredList = this.ingredients
      this.currentPage = 1
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
    },
    handleCurrentChange(val) {
      this.currentPage = val
    }
  }
}
</script>

<style scoped>
.ingredient-container {
  padding: 20px;
}

.title {
  font-size: 24px;
  font-weight: 700;
  color: #2a6f97;
  font-family: "Noto Serif SC", serif;
  padding: 8px 12px;
  background: linear-gradient(135deg, #e0f7fa 0%, #d0f0ff 100%);
  border-radius: 6px;
  margin-bottom: 10px;
  position: relative;
}

.title::before {
  content: "🧪";
  margin-right: 8px;
}

.link {
  color: #409eff;
  text-decoration: none;
  cursor: pointer;
}
.link:hover {
  text-decoration: underline;
}

.header-wrapper {
  display: flex;
  flex-direction: column;
}

.title-search {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
  margin-top: 10px;
}

.tip-container {
  align-self: flex-start;
  margin-left: 4px;
}

.search-tip {
  font-size: 13px;
  color: #888;
  line-height: 1.6;
  list-style-type: disc;
  padding-left: 20px;
  background: #f9f9f9;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
</style>