<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'



onMounted(() => {
  console.log('App mounted')
})
</script>
