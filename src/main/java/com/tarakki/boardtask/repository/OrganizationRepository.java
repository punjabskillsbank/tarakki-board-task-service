package com.tarakki.boardtask.repository;

import com.tarakki.common.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    @Query(value = """
            SELECT COUNT(*) > 0
            FROM org_members
            WHERE org_member_id = :orgMemberId
              AND org_id = :orgId
            """, nativeQuery = true)
    boolean existsOrgMemberInOrganization(@Param("orgMemberId") Long orgMemberId,
                                          @Param("orgId") Long orgId);

    @Query(value = """
            SELECT member_id
            FROM org_members
            WHERE org_member_id = :orgMemberId
              AND org_id = :orgId
            """, nativeQuery = true)
    Optional<UUID> findMemberIdByOrgMemberIdAndOrgId(@Param("orgMemberId") Long orgMemberId,
                                                     @Param("orgId") Long orgId);
}
