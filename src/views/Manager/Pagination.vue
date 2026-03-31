<template>
  <div style="text-align: center; margin-top: 20px">
    <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :current-page.sync="page"
        :page-size.sync="pageSize"
        :page-sizes="[5, 10, 20, 50]"
    />
  </div>
</template>

<script>
export default {
  props: {
    total: Number,
    page: Number,
    pageSize: Number
  }
}
</script>
