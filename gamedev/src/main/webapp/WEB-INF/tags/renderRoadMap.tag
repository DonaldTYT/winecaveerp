<%@ tag description="Render the drill-down roadmap menu" pageEncoding="UTF-8" %>
<%@ attribute name="nodes" required="true" type="java.util.List" %>
<%@ taglib prefix="app" tagdir="/WEB-INF/tags" %>

<div class="shell-roadmap" data-roadmap>
  <div class="d-flex align-items-center gap-2 mb-3 shell-roadmap-position">
    <button type="button" class="btn btn-sm btn-light d-none" data-roadmap-back>
      <i class="ti ti-arrow-left me-1"></i>Back
    </button>
    <nav aria-label="Roadmap position">
      <ol class="breadcrumb mb-0" data-roadmap-breadcrumb>
        <li class="breadcrumb-item active" aria-current="page">Top menu</li>
      </ol>
    </nav>
  </div>
  <app:renderRoadMapLevel nodes="${nodes}" levelId="root" levelTitle="Top menu"/>
</div>
