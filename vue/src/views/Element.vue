<template>
  <div class="element-demo-page">
    <!-- 1. Grid Layout Demo -->
    <el-row :gutter="20" class="demo-row">
      <el-col :span="1">
        <div class="grid-item" style="background-color: #42b983;"></div>
      </el-col>
      <el-col :span="12">
        <div class="grid-item" style="background-color: #f56c6c;"></div>
      </el-col>
    </el-row>

    <!-- 2. Button Demo -->
    <el-row :gutter="10" class="demo-row">
      <el-button round>Round Button</el-button>
      <el-button type="primary" round>Primary Button</el-button>
      <el-button type="success" round>Success Button</el-button>
      <el-button type="info" round>Info Button</el-button>
      <el-button type="warning" round>Warning Button</el-button>
      <el-button type="danger" round>Danger Button</el-button>
      <el-button type="primary" icon="el-icon-edit" circle></el-button>
      <el-button type="primary" :loading="true" round>Loading</el-button>
      <el-button circle icon="el-icon-edit" type="primary"></el-button>
      <el-button circle icon="el-icon-delete" type="danger"></el-button>
    </el-row>

    <!-- 3. Input, Autocomplete, Select, Radio -->
    <el-row :gutter="20" class="demo-row">
      <el-col :span="6">
        <el-input
          style="width: 100%"
          show-password
          suffix-icon="el-icon-search"
          v-model="value"
          placeholder="Password Input"
        ></el-input>
      </el-col>
      <el-col :span="6">
        <el-input
          style="width: 100%"
          clearable
          suffix-icon="el-icon-search"
          v-model="clear"
          placeholder="Clearable Input"
        ></el-input>
      </el-col>

      <el-col :span="6">
        <div class="sub-title">Match Suggestions After Input</div>
        <el-autocomplete
          class="inline-input"
          v-model="state2"
          :fetch-suggestions="querySearch"
          placeholder="Please Enter Content"
          :trigger-on-focus="false"
          @select="handleSelect"
        ></el-autocomplete>
      </el-col>

      <el-col :span="6">
        <el-select
          v-model="select1"
          placeholder="Please Select"
          @change="changeFood"
          filterable
          style="width: 100%"
        >
          <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          ></el-option>
        </el-select>
      </el-col>

      <el-col :span="6" style="margin-top: 20px;">
        <el-radio-group v-model="select2" @change="changeRadio">
          <el-radio label="Male">Male</el-radio>
          <el-radio label="Female">Female</el-radio>
        </el-radio-group>
      </el-col>
    </el-row>

    <!-- 4. Date Picker -->
    <el-row :gutter="20" class="demo-row">
      <el-col :span="12">
        <span class="demonstration">Date Selection</span>
        <el-date-picker
          v-model="value1"
          type="date"
          placeholder="Select Date"
          value-format="yyyy-MM-dd"
          @change="handleDateChange"
          style="width: 100%"
        ></el-date-picker>
      </el-col>
      <el-col :span="12">
        <span class="demonstration">Date Range</span>
        <el-date-picker
          v-model="value2"
          type="daterange"
          range-separator="To"
          start-placeholder="Start Date"
          end-placeholder="End Date"
          value-format="yyyy-MM-dd"
          @change="handleDateRangeChange"
          style="width: 100%"
        ></el-date-picker>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'ElementDemo',
  data() {
    return {
      // Autocomplete Data Source
      restaurants: [
        { value: 'Sanquan Fresh Food (Beixinjing Branch)', address: 'No.144 Xinyu Road, Changning District' },
        { value: 'Hot honey Seoul Fried Chicken (Xianxia Road)', address: 'No.661 Songhong Road, Changning District, Shanghai' },
        { value: 'Starbucks (Dragon Dream Branch)', address: 'No.1018 Changning Road, Changning District' },
        { value: 'Haidilao Hot Pot (Global Harbor Branch)', address: 'No.3300 North Zhongshan Road, Putuo District' }
      ],
      value: '',
      clear: '',
      state2: '',
      select1: '',
      select2: '',
      value1: '',
      value2: '',
      options: [
        { value: '1', label: 'Golden Cake' },
        { value: '2', label: 'Twin Skin Milk' },
        { value: '3', label: 'Oyster Omelette' },
        { value: '4', label: 'Dragon Beard Noodles' },
        { value: '5', label: 'Peking Duck' }
      ]
    }
  },
  methods: {
    // Autocomplete Search Logic
    querySearch(queryString, cb) {
      const results = queryString
        ? this.restaurants.filter(item => item.value.includes(queryString))
        : this.restaurants
      cb(results)
    },
    // Select Autocomplete Item
    handleSelect(item) {
      console.log('Selected suggestion:', item)
      this.$message.success(`You selected: ${item.value}`)
    },
    // Dropdown Select Change
    changeFood(val) {
      console.log('Selected food:', val)
      const target = this.options.find(item => item.value === val)
      this.$message.info(`You selected: ${target?.label || 'None'}`)
    },
    // Radio Change
    changeRadio(val) {
      console.log('Selected gender:', val)
      this.$message.success(`Gender: ${val}`)
    },
    // Single Date Change
    handleDateChange(val) {
      console.log('Selected date:', val)
      this.$message.success(`Date: ${val}`)
    },
    // Date Range Change
    handleDateRangeChange(val) {
      console.log('Selected date range:', val)
      if (val && val.length === 2) {
        this.$message.success(`Start: ${val[0]}, End: ${val[1]}`)
      }
    }
  }
}
</script>

<style scoped>
.element-demo-page {
  padding: 30px;
  background-color: #f5f7fa;
  min-height: 100vh;
}
.demo-row {
  margin-bottom: 30px;
  align-items: center;
}
.grid-item {
  height: 120px;
  border-radius: 4px;
}
.sub-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}
.demonstration {
  display: block;
  margin-bottom: 8px;
  color: #666;
}
.inline-input {
  width: 100%;
}
</style>