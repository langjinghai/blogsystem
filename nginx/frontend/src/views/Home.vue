<template>
  <div class="app-home">
    <!-- ====== 英雄区 ====== -->
    <section class="app-hero">
      <div class="app-hero__glow"></div>
      <div class="app-hero__body">
        <div class="app-hero__badge">v3.7.1 — 神经矩阵已就绪</div>
        <h1 class="app-hero__title">
          <span class="app-hero__title-row">AIOT <em>Nexus</em></span>
          <span class="app-hero__title-sub">神经矩阵指挥系统</span>
        </h1>
        <p class="app-hero__desc">
          探索前沿 AI 与物联网技术，连接数据与智能的无限可能。<br />
          发布你的见解，与全球指挥官共同构建知识矩阵。
        </p>
        <div class="app-hero__actions">
          <router-link to="/article/list" class="app-hero-btn app-hero-btn--primary">
            探索文章
            <span class="app-hero-btn__arrow">→</span>
          </router-link>
          <router-link to="/article/publish" class="app-hero-btn app-hero-btn--ghost">
            发布内容
          </router-link>
        </div>
      </div>
    </section>

    <!-- ====== 特色区 ====== -->
    <section class="app-features">
      <div class="app-feature-card" v-for="card in featureCards" :key="card.title">
        <div class="app-feature-card__icon">{{ card.icon }}</div>
        <h3 class="app-feature-card__title">{{ card.title }}</h3>
        <p class="app-feature-card__desc">{{ card.desc }}</p>
      </div>
    </section>

  </div>
</template>

<script setup lang="ts">
const featureCards = [
  {
    icon: '🧠',
    title: '深度洞见',
    desc: '来自一线开发者的 AI、IoT 技术分享与实战经验，助你快速掌握前沿技术方向。',
  },
  {
    icon: '🤝',
    title: '指挥协作',
    desc: '与全球技术指挥官互动讨论，评论、点赞，共同打磨每一个技术方案。',
  },
  {
    icon: '⚡',
    title: '极速发布',
    desc: 'Markdown 编辑器 + 图片上传，从灵感到发布只需几分钟。',
  },
  {
    icon: '🛡️',
    title: '数据主权',
    desc: '你的创作数据完全自主掌控。AES-256 加密传输，安全如堡垒。',
  },
]
</script>

<style lang="scss" scoped>
// ==================== 容器 ====================
.app-home {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 0 48px;
}

// ==================== 英雄区 ====================
.app-hero {
  position: relative;
  text-align: center;
  padding: 56px 20px 48px;
  margin-bottom: 48px;

  &__glow {
    position: absolute;
    top: -80px;
    left: 50%;
    transform: translateX(-50%);
    width: 500px;
    height: 500px;
    background: radial-gradient(circle, var(--color-primary-glow) 0%, transparent 70%);
    pointer-events: none;
  }

  &__body {
    position: relative;
    z-index: 1;
  }

  &__badge {
    display: inline-block;
    padding: 6px 18px;
    border-radius: 20px;
    border: 1px solid var(--border-glow);
    background: var(--color-primary-glow);
    font-size: 12px;
    font-family: var(--font-mono);
    color: var(--color-primary);
    letter-spacing: 1px;
    margin-bottom: 28px;
  }

  &__title {
    margin: 0 0 20px;

    &-row {
      display: block;
      font-size: 52px;
      font-weight: 900;
      letter-spacing: 4px;
      color: var(--text-primary);
      line-height: 1.15;

      em {
        font-style: normal;
        background: linear-gradient(90deg, var(--color-primary), var(--color-accent));
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }
    }

    &-sub {
      display: block;
      font-size: 18px;
      font-weight: 400;
      color: var(--text-secondary);
      letter-spacing: 6px;
      margin-top: 8px;
    }
  }

  &__desc {
    font-size: 15px;
    color: var(--text-secondary);
    line-height: 1.8;
    margin: 0 auto 36px;
    max-width: 520px;
  }

  &__actions {
    display: flex;
    gap: 16px;
    justify-content: center;
    flex-wrap: wrap;
  }
}

// ==================== 按钮 ====================
.app-hero-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.2s;
  cursor: pointer;
  border: none;

  &--primary {
    background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
    color: #fff;
    box-shadow: 0 0 24px var(--color-primary-glow);

    &:hover {
      box-shadow: 0 0 36px var(--color-primary-glow);
      transform: translateY(-1px);
    }
  }

  &--ghost {
    background: transparent;
    color: var(--text-secondary);
    border: 1px solid var(--border-card);

    &:hover {
      border-color: var(--border-glow);
      color: var(--text-primary);
      background: var(--color-primary-glow);
    }
  }

  &__arrow {
    transition: transform 0.2s;
  }

  &--primary:hover &__arrow {
    transform: translateX(3px);
  }
}

// ==================== 特色卡片 ====================
.app-features {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 48px;

  @media (max-width: 900px) {
    grid-template-columns: repeat(2, 1fr);
  }
  @media (max-width: 500px) {
    grid-template-columns: 1fr;
  }
}

.app-feature-card {
  background: var(--bg-card);
  border: 1px solid var(--border-card);
  border-radius: 14px;
  padding: 28px 22px;
  text-align: center;
  transition: all 0.25s;

  &:hover {
    border-color: var(--border-glow);
    background: var(--bg-card-hover);
    transform: translateY(-2px);
    box-shadow: var(--shadow-card-hover);
  }

  &__icon {
    font-size: 32px;
    margin-bottom: 14px;
  }

  &__title {
    font-size: 15px;
    font-weight: 700;
    color: var(--text-primary);
    margin: 0 0 8px;
  }

  &__desc {
    font-size: 13px;
    color: var(--text-muted);
    line-height: 1.6;
    margin: 0;
  }
}

</style>
